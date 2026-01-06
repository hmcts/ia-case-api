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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class AppealTypeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public AppealTypeHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

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

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (HandlerUtils.hasRepresentation(asylumCase)
                && !HandlerUtils.isInternalCase(asylumCase)
                && !HandlerUtils.isAppellantsRepresentation(asylumCase)
                && HandlerUtils.hasUpdatedLegalRepFields(callback)) {
            asylumCase.write(HAS_ADDED_LEGAL_REP_DETAILS, YesOrNo.YES);
        } else {
            asylumCase.write(HAS_ADDED_LEGAL_REP_DETAILS, YesOrNo.NO);
            HandlerUtils.clearLegalRepFields(asylumCase);
        }

        Optional<YesOrNo> isNabaEnabled = asylumCase.read(IS_NABA_ENABLED, YesOrNo.class);

        if (callback.getEvent() == START_APPEAL && isNabaEnabled.isEmpty()) {
            // (Pre NABA ccd release) checking for empty, as when the flag is disabled, it is not visible on any screens hence not part of the case data unless we submit.
            // so repopulating the fields in case of start appeal.
            YesOrNo isNabaEnabledFlag
                = featureToggler.getValue("naba-feature-flag", false) ? YES : NO;
            asylumCase.write(IS_NABA_ENABLED, isNabaEnabledFlag);
            asylumCase.write(IS_NABA_ENABLED_OOC, isNabaEnabledFlag);
            YesOrNo isAdaEnabled
                = featureToggler.getValue("naba-ada-feature-flag", false) ? YES : NO;
            asylumCase.write(IS_NABA_ADA_ENABLED, isAdaEnabled);
            isNabaEnabled = Optional.of(isNabaEnabledFlag);
        }

        Optional<YesOrNo> isOocEnabled = asylumCase.read(IS_OUT_OF_COUNTRY_ENABLED, YesOrNo.class);
        if (callback.getEvent() == START_APPEAL && isOocEnabled.isEmpty()) {
            YesOrNo isOutOfCountryEnabled
                = featureToggler.getValue("out-of-country-feature", false) ? YES : NO;
            asylumCase.write(IS_OUT_OF_COUNTRY_ENABLED, isOutOfCountryEnabled);
        }

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
