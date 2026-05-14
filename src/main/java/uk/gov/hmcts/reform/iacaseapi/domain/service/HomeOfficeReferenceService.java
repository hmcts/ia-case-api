package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    public HomeOfficeReferenceService(HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.homeOfficeApi = homeOfficeApi;
    }

    // Note: don't cache this response, as we want to get fresh data each time in case something changes at the Home Office's end.
    public Optional<List<IdValue<HomeOfficeAppellant>>> getHomeOfficeReferenceData(String hoReference, Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellants = asylumCase.read(HOME_OFFICE_APPELLANTS);
        // If we have a list of appellants already, don't call the API again.
        if (homeOfficeAppellants.isPresent()) {
            log.info("[HOME OFFICE CACHE HIT] Returning {} appellant(s) already stored in case data for HO reference {}.",
                homeOfficeAppellants.get().size(), hoReference);
            return homeOfficeAppellants;
        }

        // TEMP: return dummy appellant to skip Home Office API call during local investigation
        log.info("[HOME OFFICE DUMMY DATA] Returning dummy appellant for HO reference {}.", hoReference);
        HomeOfficeAppellant dummy = new HomeOfficeAppellant();
        dummy.setFamilyName(asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("Smith"));
        dummy.setGivenNames(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("John"));
        dummy.setDateOfBirth(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("1990-01-01"));
        homeOfficeAppellants = Optional.of(List.of(new IdValue<>("1", dummy)));
        asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants);
        asylumCase.write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.OK);
        return homeOfficeAppellants;

        // Home Office API has not been called yet (or was unavailable the last time we tried) - call it now
        // log.info("[HOME OFFICE API CALL] No cached data found for HO reference {} — calling ia-home-office-integration-api now.", hoReference);
        // Raise an event in home-office-integration-api

        // AsylumCase asylumCaseWithHomeOfficeData = homeOfficeApi.midEvent(callback);
        // Check return status and store it in the case record
        // HomeOfficeApiResponseStatusType responseStatus =
        //                                 asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
        //                                 .orElse(HomeOfficeApiResponseStatusType.UNKNOWN);
        // asylumCase.write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, responseStatus);
        // if (responseStatus.equals(HomeOfficeApiResponseStatusType.OK)) {
        //     homeOfficeAppellants = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANTS);
        //     log.info("[HOME OFFICE API CALL] Successfully retrieved {} appellant(s) from ia-home-office-integration-api for HO reference {}.",
        //         homeOfficeAppellants.map(List::size).orElse(0), hoReference);
        //     // Update the case record object with the Home Office reference data
        //     asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants);
        // } else {
        //     // The API did not return any data; log the diagnostic message appropriately
        //     String logMessage = "Biographic information from Home Office asylum (etc.) application with Home Office reference "
        //                       + hoReference + " could not be retrieved.\n\n" + responseStatus.getHoIntegrationErrorText(hoReference)
        //                       + "\n\nSee the corresponding logs in ia-home-office-integration-api for more details.";
        //     int statusCode = responseStatus.getStatusCode();
        //     if (statusCode == 404) {
        //         log.info(logMessage);
        //     } else if (statusCode <= 0 || statusCode >= 500) {
        //         log.warn(logMessage);
        //     } else {
        //         log.error(logMessage);
        //     }
        // }
        // return homeOfficeAppellants;


        //Probably this need to be amended to persist in the asylmy case from ccd api not home office api
    }

}
