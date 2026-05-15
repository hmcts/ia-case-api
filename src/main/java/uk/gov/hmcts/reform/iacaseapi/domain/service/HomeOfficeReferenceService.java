package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
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
        if (!homeOfficeAppellants.isEmpty() && !homeOfficeAppellants.get().isEmpty()) {
            log.info("Returning previously retrieved Home Office reference data for case with Home Office reference {}.", hoReference);
            return homeOfficeAppellants;
        }

        // Home Office API has not been called yet (or was unavailable the last time we tried) - call it now
        log.info("Getting Home Office biographic data for case with reference ID {} ...", hoReference);
        // Raise an event in home-office-integration-api
        AsylumCase asylumCaseWithHomeOfficeData = homeOfficeApi.midEvent(callback);
        // Check return status and store it in the case record
        HomeOfficeApiResponseStatusType responseStatus = 
                                        asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                                        .orElse(HomeOfficeApiResponseStatusType.UNKNOWN);
        asylumCase.write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, responseStatus);
        if (responseStatus.equals(HomeOfficeApiResponseStatusType.OK)) {
            log.info("Home Office biographic data retrieved for case with reference ID {}.", hoReference);
            // Update the case record object with the Home Office reference data
            homeOfficeAppellants = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANTS);
            asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants);
            asylumCase.write(HOME_OFFICE_APPELLANT_CLAIM_DATE, asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_CLAIM_DATE, String.class).orElse(null));
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_DATE, asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_DECISION_DATE, String.class).orElse(null));
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, String.class).orElse(null));
        } else {
            // The API did not return any data; log the diagnostic message appropriately
            String logMessage = "Biographic information from Home Office asylum (etc.) application with Home Office reference "
                              + hoReference + " could not be retrieved.\n\n" + responseStatus.getHoIntegrationErrorText(hoReference)
                              + "\n\nSee the corresponding logs in ia-home-office-integration-api for more details.";                    
            int statusCode = responseStatus.getStatusCode();
            if (statusCode == 404) {
                // User error (appeal not found)
                log.info(logMessage);
            } else if (statusCode <= 0 || statusCode >= 500) {
                // Server error
                log.warn(logMessage);
            } else {
                // Client error
                log.error(logMessage);
            }
        }
        return homeOfficeAppellants;
    }

}
