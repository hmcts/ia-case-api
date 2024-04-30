package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ACTUAL_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_TCW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DOES_THE_CASE_NEED_TO_BE_RELISTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EPIMS_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CONDUCTION_OPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEWED_UPDATED_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;


@Component
public class ListEditCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final CaseManagementLocationService caseManagementLocationService;
    private final LocationRefDataService locationRefDataService;

    public ListEditCaseHandler(HearingCentreFinder hearingCentreFinder,
                               CaseManagementLocationService caseManagementLocationService,
                               LocationRefDataService locationRefDataService) {
        this.hearingCentreFinder = hearingCentreFinder;
        this.caseManagementLocationService = caseManagementLocationService;
        this.locationRefDataService = locationRefDataService;
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

        // to keep LISTING_LOCATION aligned with LIST_CASE_HEARING_CENTRE until all code uses LISTING_LOCATION
        if (isCaseUsingLocationRefData(asylumCase)) {
            asylumCase.read(LISTING_LOCATION, DynamicList.class)
                .ifPresent(dynamicList -> {
                    String epimsId = dynamicList.getValue().getCode();
                    HearingCentre.from(epimsId).ifPresent(hc -> asylumCase.write(LIST_CASE_HEARING_CENTRE, hc));
                });
        }

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

        asylumCase.write(LIST_CASE_HEARING_CENTRE_ADDRESS, locationRefDataService
            .getHearingCentreAddress(listCaseHearingCentre));
        asylumCase.write(EPIMS_ID, HearingCentre.getEpimsIdByValue(listCaseHearingCentre.getValue()));
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
