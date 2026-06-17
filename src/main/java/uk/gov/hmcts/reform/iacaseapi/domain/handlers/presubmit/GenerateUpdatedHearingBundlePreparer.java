package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isNotificationTurnedOff;

@Component
public class GenerateUpdatedHearingBundlePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentGenerator<AsylumCase> documentGenerator;

    public GenerateUpdatedHearingBundlePreparer(
        DocumentGenerator<AsylumCase> documentGenerator
    ) {
        this.documentGenerator = documentGenerator;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isNotificationTurnedOff(asylumCase)) {
            return false;
        }

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
               && callback.getEvent() == Event.GENERATE_UPDATED_HEARING_BUNDLE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        State currentState = callback.getCaseDetails().getState();
        String stateBeforeAdjourned = asylumCase
            .read(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class)
            .orElse("");
        if (currentState == State.ADJOURNED && !(stateBeforeAdjourned.equals(State.PRE_HEARING.toString())
            || stateBeforeAdjourned.equals(State.DECISION.toString()))) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("Case was adjourned before the initial hearing bundle was created.");
            return response;
        }

        return new PreSubmitCallbackResponse<>(documentGenerator.aboutToStart(callback));
    }

}
