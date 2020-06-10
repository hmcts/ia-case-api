package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_CASE_STATUS_DATA;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.ApplicationStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@Component
public class HomeOfficeCaseValidateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;
    private final boolean isHomeOfficeIntegrationEnabled;

    public HomeOfficeCaseValidateHandler(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled,
        HomeOfficeApi<AsylumCase> homeOfficeApi) {
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
            && callback.getEvent() == Event.SUBMIT_APPEAL
            && callback.getCaseDetails().getState() == State.APPEAL_SUBMITTED;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithHomeOfficeData = homeOfficeApi.call(callback);

        Optional<HomeOfficeCaseStatus> existingCaseStatusData =
            asylumCaseWithHomeOfficeData.read(HOME_OFFICE_CASE_STATUS_DATA);

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
                homeOfficeCaseStatus.getDisplayMetadataValueDateTime()
            );

            asylumCaseWithHomeOfficeData.write(HOME_OFFICE_CASE_STATUS_DATA, modifiedHomeOfficeCaseStatus);

        }

        Optional<ContactPreference> contactPreference = asylumCaseWithHomeOfficeData.read(CONTACT_PREFERENCE);
        if (contactPreference.isPresent()) {
            asylumCaseWithHomeOfficeData.write(
                CONTACT_PREFERENCE_DESCRIPTION, contactPreference.get().getDescription());
        }

        Optional<AppealType> appealType = asylumCaseWithHomeOfficeData.read(APPEAL_TYPE);
        if (appealType.isPresent()) {
            asylumCaseWithHomeOfficeData.write(
                APPEAL_TYPE_DESCRIPTION, appealType.get().getDescription());
        }

        return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
    }
}
