package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.ApplicationStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@Component
public class HomeOfficeCaseValidateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;
    private final boolean isHomeOfficeIntegrationEnabled;
    private final FeatureToggler featureToggler;
    private static final String HO_NOTIFICATION_FEATURE = "home-office-notification-feature";

    public HomeOfficeCaseValidateHandler(
        FeatureToggler featureToggler,
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled,
        HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.featureToggler = featureToggler;
        this.isHomeOfficeIntegrationEnabled = isHomeOfficeIntegrationEnabled;
        this.homeOfficeApi = homeOfficeApi;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return isHomeOfficeIntegrationEnabled
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        if (asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
                .map(value -> value.equals(YesOrNo.YES)).orElse(true)
                && HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType)) {

            asylumCase.write(IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);

            boolean isNotificationTurnedOff = HandlerUtils.isNotificationTurnedOff(asylumCase);

            if (HandlerUtils.isEjpCase(asylumCase) || HandlerUtils.isAgeAssessmentAppeal(asylumCase)
                    || isNotificationTurnedOff) {
                return new PreSubmitCallbackResponse<>(asylumCase);
            }

            asylumCase =
                featureToggler.getValue("home-office-uan-feature", false)
                    ? homeOfficeApi.aboutToSubmit(callback) : homeOfficeApi.call(callback);

            Optional<ContactPreference> contactPreference = asylumCase.read(CONTACT_PREFERENCE);
            if (contactPreference.isPresent()) {
                asylumCase.write(
                    CONTACT_PREFERENCE_DESCRIPTION, contactPreference.get().getDescription());
            }

            asylumCase.write(
                    APPEAL_TYPE_DESCRIPTION, appealType.getDescription());

            Optional<HomeOfficeCaseStatus> existingCaseStatusData =
                asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA);
            if (existingCaseStatusData.isPresent()
                && existingCaseStatusData.get().getApplicationStatus() != null
            ) {
                final HomeOfficeCaseStatus homeOfficeCaseStatus = existingCaseStatusData.get();
                final ApplicationStatus applicationStatus = homeOfficeCaseStatus.getApplicationStatus();
                applicationStatus.modifyListDataForCcd();

                HomeOfficeCaseStatus modifiedHomeOfficeCaseStatus = new HomeOfficeCaseStatus(
                    homeOfficeCaseStatus.getPerson(),
                    applicationStatus,
                    homeOfficeCaseStatus.getDisplayDateOfBirth(),
                    homeOfficeCaseStatus.getDisplayRejectionReasons(),
                    homeOfficeCaseStatus.getDisplayDecisionDate(),
                    homeOfficeCaseStatus.getDisplayDecisionSentDate(),
                    homeOfficeCaseStatus.getDisplayMetadataValueBoolean(),
                    homeOfficeCaseStatus.getDisplayMetadataValueDateTime(),
                    "<h2>Appellant details</h2>",
                    "<h2>Application details</h2>"
                );

                asylumCase.write(HOME_OFFICE_CASE_STATUS_DATA, modifiedHomeOfficeCaseStatus);
            }

            Optional<List<IdValue<NationalityFieldValue>>> nationalities = asylumCase.read(
                APPELLANT_NATIONALITIES);

            StringBuilder nationalitiesForDisplay = new StringBuilder("");
            if (nationalities.isPresent()) {
                nationalities.get().stream()
                    .map(idValue -> Nationality.valueOf(idValue.getValue().getCode()).toString())
                    .distinct()
                    .collect(Collectors.toList())
                    .forEach(
                        nn -> {
                            nationalitiesForDisplay.append(nn).append("<br />");
                        });

                nationalitiesForDisplay.delete(
                    nationalitiesForDisplay.lastIndexOf("<br />"), nationalitiesForDisplay.length());
            }
            asylumCase.write(APPELLANT_NATIONALITIES_DESCRIPTION, nationalitiesForDisplay.toString());
            asylumCase.write(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE,
                featureToggler.getValue(HO_NOTIFICATION_FEATURE, false) ? YesOrNo.YES : YesOrNo.NO);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }


}
