package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
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

        HandlerUtils.formatHearingAdjustmentResponses(asylumCase);

        asylumCase.write(AsylumCaseFieldDefinition.REVIEWED_UPDATED_HEARING_REQUIREMENTS, YesOrNo.YES);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, callback.getCaseDetails().getState());
        asylumCase.clear(DISABLE_OVERVIEW_PAGE);
        asylumCase.clear(UPDATE_HEARING_REQUIREMENTS_EXISTS);
        // since we are re-using the fields from SHR and CCD does not have option to specify AND and OR combination
        // in fieldShowCondition, we set the original flag as well here for right rendering.
        asylumCase.write(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        if (isAcceleratedDetainedAppeal) {
            //For ADA case type - Clear flag to remove access to adjust hearing requirements event
            asylumCase.clear(ADA_HEARING_ADJUSTMENTS_UPDATABLE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
