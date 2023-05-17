package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CompanyNameProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class AppealOutOfCountryPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final CompanyNameProvider companyNameProvider;
    private final FeatureToggler featureToggler;

    public AppealOutOfCountryPreparer(
        CompanyNameProvider companyNameProvider,
        FeatureToggler featureToggler
    ) {
        this.companyNameProvider = companyNameProvider;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.CREATE_DLRM_CASE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isOutOfCountryEnabled
            = featureToggler.getValue("out-of-country-feature", false) ? YesOrNo.YES : YesOrNo.NO;
        asylumCase.write(IS_OUT_OF_COUNTRY_ENABLED, isOutOfCountryEnabled);

        if (isOutOfCountryEnabled.equals(YesOrNo.NO)) {
            asylumCase.write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        }

        companyNameProvider.prepareCompanyName(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
