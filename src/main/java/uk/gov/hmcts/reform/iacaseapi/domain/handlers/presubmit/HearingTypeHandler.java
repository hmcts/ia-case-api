package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealTypeForDisplay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HearingTypeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.EDIT_APPEAL);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElse(null);
        AppealTypeForDisplay appealTypeForDisplay = asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)
            .orElse(null);

        //if RP or DC or ADA

        boolean isRpDcAda = appealTypeForDisplay != null
                            && (appealTypeForDisplay == AppealTypeForDisplay.DC || appealTypeForDisplay == AppealTypeForDisplay.RP
                                || asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO) == YES);

        //if RP or DC or ADA

        if (callback.getEvent() == Event.START_APPEAL) {
            YesOrNo hearingTypeResult = isRpDcAda ? YES : NO;
            asylumCase.write(HEARING_TYPE_RESULT, hearingTypeResult);
        }

        if (callback.getEvent() == Event.EDIT_APPEAL) {

            boolean isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO).equals(YES);

            if (isRpDcAda && !isAgeAssessmentAppeal) {
                asylumCase.write(HEARING_TYPE_RESULT, YES);
            } else {
                asylumCase.write(HEARING_TYPE_RESULT, YesOrNo.NO);
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
