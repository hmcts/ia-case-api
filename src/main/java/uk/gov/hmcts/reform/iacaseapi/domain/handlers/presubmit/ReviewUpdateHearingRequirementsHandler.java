package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewUpdateHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPDATE_HEARING_ADJUSTMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        formatHearingAdjustmentResponses(asylumCase);

        asylumCase.write(AsylumCaseFieldDefinition.REVIEWED_UPDATED_HEARING_REQUIREMENTS, YesOrNo.YES);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, callback.getCaseDetails().getState());
        asylumCase.clear(DISABLE_OVERVIEW_PAGE);
        asylumCase.clear(UPDATE_HEARING_REQUIREMENTS_EXISTS);
        // since we are re-using the fields from SHR and CCD does not have option to specify AND and OR combination
        // in fieldShowCondition, we set the original flag as well here for right rendering.
        asylumCase.write(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void formatHearingAdjustmentResponses(AsylumCase asylumCase) {
        formatHearingAdjustmentResponse(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE, IS_VULNERABILITIES_ALLOWED)
                .ifPresent(response -> asylumCase.write(VULNERABILITIES_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, IS_REMOTE_HEARING_ALLOWED)
                .ifPresent(response -> asylumCase.write(REMOTE_HEARING_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, MULTIMEDIA_TRIBUNAL_RESPONSE, IS_MULTIMEDIA_ALLOWED)
                .ifPresent(response -> asylumCase.write(MULTIMEDIA_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, IS_SINGLE_SEX_COURT_ALLOWED)
                .ifPresent(response -> asylumCase.write(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, IN_CAMERA_COURT_TRIBUNAL_RESPONSE, IS_IN_CAMERA_COURT_ALLOWED)
                .ifPresent(response -> asylumCase.write(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, ADDITIONAL_TRIBUNAL_RESPONSE, IS_ADDITIONAL_ADJUSTMENTS_ALLOWED)
                .ifPresent(response -> asylumCase.write(OTHER_DECISION_FOR_DISPLAY, response));
    }

    private Optional<String> formatHearingAdjustmentResponse(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition responseDefinition,
            AsylumCaseFieldDefinition decisionDefinition) {
        String response = asylumCase.read(responseDefinition, String.class).orElse(null);
        String decision = asylumCase.read(decisionDefinition, String.class).orElse(null);

        return !(response == null || decision == null) ? Optional.of(decision + " - " + response) : Optional.empty();
    }
}
