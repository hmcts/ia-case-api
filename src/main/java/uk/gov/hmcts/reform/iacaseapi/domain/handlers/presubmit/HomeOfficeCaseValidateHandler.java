package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HOME_OFFICE_INTEGRATION_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return isHomeOfficeIntegrationEnabled
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == SUBMIT_APPEAL
            || callback.getEvent() == PAY_AND_SUBMIT_APPEAL
            || callback.getEvent() == MARK_APPEAL_PAID
            || callback.getEvent() == REQUEST_HOME_OFFICE_DATA);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithHomeOfficeData = homeOfficeApi.call(callback);

        asylumCaseWithHomeOfficeData.write(IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);

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
                homeOfficeCaseStatus.getDisplayMetadataValueDateTime(),
                "<h2>Appellant details</h2>",
                "<h2>Application details</h2>"
            );

            asylumCaseWithHomeOfficeData.write(HOME_OFFICE_CASE_STATUS_DATA, modifiedHomeOfficeCaseStatus);
        }

        Optional<List<IdValue<NationalityFieldValue>>> nationalities = asylumCaseWithHomeOfficeData.read(
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
        asylumCaseWithHomeOfficeData.write(APPELLANT_NATIONALITIES_DESCRIPTION, nationalitiesForDisplay.toString());

        return new PreSubmitCallbackResponse<>(asylumCaseWithHomeOfficeData);
    }


}
