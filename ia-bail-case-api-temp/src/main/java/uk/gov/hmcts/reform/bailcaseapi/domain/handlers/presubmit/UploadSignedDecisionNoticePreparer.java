package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UploadSignedDecisionNoticePreparer implements PreSubmitCallbackHandler<BailCase> {

    public static final String MISSING_DECISION_ERROR_MESSAGE = "Decision type is missing. Please record the decision " +
        "before uploading the signed decision notice.";
    public static final String INVALID_EVENT_ERROR_MESSAGE = "This event is invalid for this decision type. Please " +
        "use the 'Upload signed decision notice' event from the next step dropdown.";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && (callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE
            || callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();
        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        Optional<String> optionalDecisionType =
            bailCase.read(RECORD_DECISION_TYPE, String.class);

        if (optionalDecisionType.isEmpty()) {
            response.addError(MISSING_DECISION_ERROR_MESSAGE);
            return response;
        }

        DecisionType decisionType = DecisionType.getEnum(optionalDecisionType.get());
        if (!decisionType.isValidFor(callback.getEvent())) {
            response.addError(INVALID_EVENT_ERROR_MESSAGE);
        }
        return response;
    }
}
