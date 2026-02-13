package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    private final CcdDataService ccdDataService;

    public HomeOfficeReferenceService(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    // Note: don't cache this response, as we want to get fresh data each time in case something changes at the Home Office's end.
    public Optional<List<HomeOfficeAppellant>> getHomeOfficeReferenceData(String reference, long caseId, AsylumCase asylumCase) {

        Optional<List<HomeOfficeAppellant>> homeOfficeAppellants = asylumCase.read(HOME_OFFICE_APPELLANTS);
        // If we have a list of appellants already, don't call the API again.
        if (homeOfficeAppellants.isPresent()) {
            return homeOfficeAppellants;
        }

        // Home Office API has not been called yet (or was unavailable the last time we tried) - call it now
        log.info("Getting Home Office reference data for case ID: {}", caseId);
        // First raise an event here in ia-case-api that will then be propagated to home-office-integration-api in a separate handler
        ccdDataService.raiseEvent(caseId, Event.GET_HOME_OFFICE_APPELLANT_DATA);
        // Check return status
        String httpStatus = asylumCase.read(HOME_OFFICE_APPELLANT_API_HTTP_STATUS, String.class).orElse("");
        if (httpStatus.equals("200")) {
            return asylumCase.read(HOME_OFFICE_APPELLANTS);
        } else {
            // Throw new exception to be caught by the event handler
            String message = "Biographic information from Home Office application with HMCTS reference " + reference + " could not be retrieved.";
            int statusCode = 0;
            try {
                statusCode = Integer.parseInt(httpStatus);
            } catch (NumberFormatException e) {
                log.warn("HTTP return status code was missing from call to the Home Office validation API.");
            }
            switch (statusCode) {
                case -1:
                    message += "\n\nThe Home Office validation API did not respond.";
                    break;
                case 0:
                    message += "\n\nThe response from the Home Office validation API could not be found.";
                    break;
                case 400:
                    message += "\n\nThe request to the Home Office validation API was not correctly formed.";
                    break;
                case 401:
                    message += "\n\nThe request to the Home Office validation API could not be authenticated.";
                    break;
                case 403:
                    message += "\n\nThe request to the Home Office validation API was authenticated but not authorised.";
                    break;
                case 404:
                    message += "\n\nNo application matching this HMCTS reference number was found.";
                    break;
                case 500:
                case 501:
                case 502:
                case 503:
                case 504:
                    message += "\n\nThe Home Office validation API was not available.";
                    break;            
                default:
                    message += "\n\nThe HTTP status code was " + String.valueOf(statusCode) + ".";
                    break;
            }
            throw new HomeOfficeMissingApplicationException(statusCode, message);            
        }
    }

}
