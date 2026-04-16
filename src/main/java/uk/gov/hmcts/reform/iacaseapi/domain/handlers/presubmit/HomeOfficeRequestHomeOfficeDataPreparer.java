package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class HomeOfficeRequestHomeOfficeDataPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isHomeOfficeIntegrationEnabled;

    private final FeatureToggler featureToggler;

    public HomeOfficeRequestHomeOfficeDataPreparer(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled,
        FeatureToggler featureToggler) {
        this.isHomeOfficeIntegrationEnabled = isHomeOfficeIntegrationEnabled;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return isHomeOfficeIntegrationEnabled
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == REQUEST_HOME_OFFICE_DATA;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (HandlerUtils.isAgeAssessmentAppeal(asylumCase)) {
            response.addError("You cannot request Home Office data for this appeal");

            return response;
        }

        //Check if home office call is made when data is already fetched
        YesOrNo isAppealOutOfCountry = asylumCase.read(AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class)
                .orElse(NO);

        AppealType appealType = asylumCase.read(AsylumCaseFieldDefinition.APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        if (!HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType)) {

            response.addError("You can only request Home Office data for an appeal against a Protection "
                    + "or Revocation of Protection decision");
            return response;
        }

        if (isAppealOutOfCountry == YES) {

            response.addError("You cannot request Home Office data for an out of country appeal");
            return response;
        }

        return response;
    }
}
