package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealTypeForDisplay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AppealTypeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && List.of(START_APPEAL, EDIT_APPEAL).contains(event);
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

        Optional<YesOrNo> isNabaEnabled = asylumCase.read(IS_NABA_ENABLED, YesOrNo.class);

        // This duplicate feature flag field is used because isNabaEnabled is on the detention screen which is not
        // visible in the OOC flows.
        Optional<YesOrNo> isNabaEnabledOoc = asylumCase.read(IS_NABA_ENABLED_OOC, YesOrNo.class);

        if (isNabaEnabled.equals(Optional.of(NO)) || isNabaEnabledOoc.equals(Optional.of(NO))) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        YesOrNo ageAssessment = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO);
        Optional<YesOrNo> appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class);
        Optional<YesOrNo> isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);

        if ((isAcceleratedDetainedAppeal.equals(Optional.of(NO)) && ageAssessment.equals(YES))
                || (appellantInDetention.equals(Optional.of(NO)) && ageAssessment.equals(YES))) {
            asylumCase.write(APPEAL_TYPE, AppealType.AG);
            asylumCase.clear(APPEAL_TYPE_FOR_DISPLAY);
        } else {
            // After release of NABA, front-end will populate APPEAL_TYPE_FOR_DISPLAY instead of APPEAL_TYPE
            // So in order to manage the FieldShowConditions we are mapping the APPEAL_TYPE to same as APPEAL_TYPE_FOR_DISPLAY
            if (!HandlerUtils.isAipJourney(asylumCase)) {

                AppealTypeForDisplay appealTypeForDisplay = asylumCase
                    .read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)
                    .orElseThrow(() -> new IllegalStateException("Appeal type not present"));
                asylumCase.write(APPEAL_TYPE, AppealType.from(appealTypeForDisplay.getValue()));
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
