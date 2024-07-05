package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@Component
public class AddBaseLocationFromHearingCentreForOldCasesFixHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CaseManagementLocationService caseManagementLocationService;

    public AddBaseLocationFromHearingCentreForOldCasesFixHandler(
        CaseManagementLocationService caseManagementLocationService) {
        this.caseManagementLocationService = caseManagementLocationService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return !Arrays.asList(
            Event.SUBMIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.CHANGE_HEARING_CENTRE,
            Event.START_APPEAL
        ).contains(callback.getEvent());
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        fixCaseManagementLocationDataIfNecessary(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void fixCaseManagementLocationDataIfNecessary(AsylumCase asylumCase) {

        if (isCaseUsingLocationRefData(asylumCase)) {
            Optional<CaseManagementLocationRefData> refDataCaseManagementLocation =
                asylumCase.read(CASE_MANAGEMENT_LOCATION_REF_DATA, CaseManagementLocationRefData.class);

            if (refDataCaseManagementLocation.isEmpty()
                || refDataCaseManagementLocation.get().getBaseLocation() == null
                || BaseLocation.GLASGOW_DEPRECATED.getId().equals(
                    refDataCaseManagementLocation.get().getBaseLocation().getValue().getCode())) {
                addBaseLocationAndStaffLocationFromHearingCentre(asylumCase, true);
            }

        } else {
            Optional<CaseManagementLocation> caseManagementLocation =
                asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class);

            if (caseManagementLocation.isEmpty()
                || caseManagementLocation.get().getBaseLocation() == null
                || BaseLocation.GLASGOW_DEPRECATED.equals(caseManagementLocation.get().getBaseLocation())) {
                addBaseLocationAndStaffLocationFromHearingCentre(asylumCase, false);
            }
        }
    }

    private void addBaseLocationAndStaffLocationFromHearingCentre(AsylumCase asylumCase, boolean isRefDataEnabled) {
        HearingCentre hearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class)
            .orElse(HearingCentre.NEWPORT);
        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION,
            isRefDataEnabled ? caseManagementLocationService.getRefDataCaseManagementLocation(staffLocationName)
                : caseManagementLocationService.getCaseManagementLocation(staffLocationName));
    }

}

