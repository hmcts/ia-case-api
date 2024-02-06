package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealEjp;

import java.util.Optional;
import org.springframework.stereotype.Component;
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

        AppealTypeForDisplay appealTypeForDisplay = asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)
            .orElse(null);

        //if RP or DC or ADA

        boolean isRpDcAdaEjp = appealTypeForDisplay != null
                            && (appealTypeForDisplay == AppealTypeForDisplay.DC || appealTypeForDisplay == AppealTypeForDisplay.RP
                                || asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO) == YES
                                || sourceOfAppealEjp(asylumCase));

        //if RP or DC or ADA

        if (callback.getEvent() == Event.START_APPEAL) {
            YesOrNo hearingTypeResult = isRpDcAdaEjp ? YES : NO;
            asylumCase.write(HEARING_TYPE_RESULT, hearingTypeResult);
        }

        if (callback.getEvent() == Event.EDIT_APPEAL) {
            Optional<YesOrNo> appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class);

            if (appellantInDetention.equals(Optional.of(NO))) {
                asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, NO);
            }

            isRpDcAdaEjp = appealTypeForDisplay != null
                                   && (appealTypeForDisplay == AppealTypeForDisplay.DC || appealTypeForDisplay == AppealTypeForDisplay.RP
                                       || asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO) == YES
                                       || sourceOfAppealEjp(asylumCase));

            Optional<YesOrNo> isAcceleratedDetainedAppeal =
                    asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);

            if (appellantInDetention.equals(Optional.of(YES))
                    && isAcceleratedDetainedAppeal.equals(Optional.of(YES))) {
                asylumCase.write(AGE_ASSESSMENT, NO);
            }

            boolean isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO).equals(YES);

            if (isRpDcAdaEjp && !isAgeAssessmentAppeal) {
                //No fee
                asylumCase.write(HEARING_TYPE_RESULT, YES);
            } else {
                //fee
                asylumCase.write(HEARING_TYPE_RESULT, YesOrNo.NO);
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
