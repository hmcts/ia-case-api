package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HOME_OFFICE_INTEGRATION_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@Component
public class HomeOfficeCaseValidatePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isHomeOfficeIntegrationEnabled;
    private final FeatureToggler featureToggler;

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    public HomeOfficeCaseValidatePreparer(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled,
        FeatureToggler featureToggler,
        HomeOfficeApi<AsylumCase> homeOfficeApi
    ) {
        this.isHomeOfficeIntegrationEnabled = isHomeOfficeIntegrationEnabled;
        this.featureToggler = featureToggler;
        this.homeOfficeApi = homeOfficeApi;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && (callback.getEvent() == SUBMIT_APPEAL
            || callback.getEvent() == MARK_APPEAL_PAID
            || callback.getEvent() == REQUEST_HOME_OFFICE_DATA);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean aaaOrDetained = HandlerUtils.isAppellantInDetention(asylumCase)
            || HandlerUtils.isAgeAssessmentAppeal(asylumCase);

        boolean isEjpCase = HandlerUtils.isEjpCase(asylumCase);

        if ((callback.getEvent() == REQUEST_HOME_OFFICE_DATA) && (aaaOrDetained || isEjpCase)) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("You cannot request Home Office data for this appeal");

            return response;
        }

        //Check if home office is enabled and set field for CCD definition
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        boolean appealTypeEnabled = HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType);

        if (!appealTypeEnabled) {

            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        if (isHomeOfficeIntegrationEnabled) {
            asylumCase.write(IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
            boolean homeOfficeUanFeatureEnabled = featureToggler.getValue("home-office-uan-feature", false);

            if (homeOfficeUanFeatureEnabled
                && appealTypeEnabled && !aaaOrDetained && !isEjpCase) {
                asylumCase = homeOfficeApi.aboutToStart(callback);
            }
        } else {
            asylumCase.write(IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.NO);
        }
        // probably not here
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
