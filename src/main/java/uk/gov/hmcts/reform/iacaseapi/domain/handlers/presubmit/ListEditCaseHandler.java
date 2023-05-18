package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;


@Component
public class ListEditCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final CaseManagementLocationService caseManagementLocationService;

    public ListEditCaseHandler(HearingCentreFinder hearingCentreFinder, CaseManagementLocationService caseManagementLocationService) {
        this.hearingCentreFinder = hearingCentreFinder;
        this.caseManagementLocationService = caseManagementLocationService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.LIST_CASE || callback.getEvent() == Event.EDIT_CASE_LISTING);
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

        HearingCentre listCaseHearingCentre =
            asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.NEWPORT);

        HearingCentre hearingCentre =
            asylumCase.read(HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.NEWPORT);

        if (!listCaseHearingCentre.equals(HearingCentre.REMOTE_HEARING)) {
            if (!hearingCentreFinder.hearingCentreIsActive(listCaseHearingCentre)) {
                asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
                asylumCase.write(HEARING_CENTRE, HearingCentre.NEWPORT);
            } else {
                if (!hearingCentreFinder.isListingOnlyHearingCentre(listCaseHearingCentre)) {
                    //Should also update the designated hearing centre
                    asylumCase.write(HEARING_CENTRE, listCaseHearingCentre);
                }
            }
        } else {
            if (!hearingCentreFinder.hearingCentreIsActive(hearingCentre)) {
                asylumCase.write(HEARING_CENTRE, HearingCentre.NEWPORT);
            }
        }

        asylumCase.write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        asylumCase.clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        asylumCase.clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);
        asylumCase.clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
        asylumCase.clear(ATTENDING_TCW);
        asylumCase.clear(ATTENDING_JUDGE);
        asylumCase.clear(ATTENDING_APPELLANT);
        asylumCase.clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        asylumCase.clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        asylumCase.clear(ACTUAL_CASE_HEARING_LENGTH);
        asylumCase.clear(HEARING_CONDUCTION_OPTIONS);
        asylumCase.clear(HEARING_RECORDING_DOCUMENTS);
        asylumCase.clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
        addBaseLocationAndStaffLocationFromHearingCentre(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addBaseLocationAndStaffLocationFromHearingCentre(AsylumCase asylumCase) {
        HearingCentre hearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class)
            .orElse(HearingCentre.TAYLOR_HOUSE);
        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION,
            caseManagementLocationService.getCaseManagementLocation(staffLocationName));
    }
}
