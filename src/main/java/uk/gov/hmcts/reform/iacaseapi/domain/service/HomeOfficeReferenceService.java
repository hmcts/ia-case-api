package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValueMixin;

import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    public HomeOfficeReferenceService(HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.homeOfficeApi = homeOfficeApi;
    }

    // Note: don't cache this response, as we want to get fresh data each time in case something changes at the Home Office's end.
    public List<IdValue<HomeOfficeAppellant>> getHomeOfficeReferenceData(String hoReference, Callback<AsylumCase> callback) {
        // We need the mapper and mix-in to overcome a CCD bug concerning collections during the mid-event (see comments below).
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(IdValue.class, IdValueMixin.class);        
        // Check case for existing data.
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String homeOfficeAppellantsSerialisedEncrypted = asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class).orElse("");
        List<IdValue<HomeOfficeAppellant>> homeOfficeAppellants = emptyList();
        // If we have a list of appellants already (in serialised form - see comments below), don't call the API again.
        if (!homeOfficeAppellantsSerialisedEncrypted.isEmpty()) {
            log.info("Deserialising and returning previously retrieved Home Office appellant data for case with Home Office reference {}.", hoReference);
            try {
                String homeOfficeAppellantsSerialised = HandlerUtils.decrypt(homeOfficeAppellantsSerialisedEncrypted);
                homeOfficeAppellants = mapper.readValue(
                                                            homeOfficeAppellantsSerialised,
                                                            new TypeReference<List<IdValue<HomeOfficeAppellant>>>() {}
                                                       );
                return homeOfficeAppellants;
            } catch (Exception ex) {
                log.error("Could not deserialise list of Home Office appellants from encrypted serialised string {} for case with Home Office reference {}:\n\n{}",
                          homeOfficeAppellantsSerialisedEncrypted, hoReference, ex.getMessage());
            }
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
            Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellantsOpt = asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANTS);
            homeOfficeAppellants = homeOfficeAppellantsOpt.orElse(emptyList());
            // asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants); -- this DOES NOT WORK due to a bug in CCD when altering collections during the mid-event.
            // Instead, we need to serialise the list and write it to a scalar (string) field; then later we will reconstitute the list from this field and store it.
            // See  HomeOfficeReferenceHandlerOnSubmit.java  for more details.
            try {
                String homeOfficeAppellantsSerialised = mapper.writerWithDefaultPrettyPrinter()
                                                       .writeValueAsString(homeOfficeAppellants);
                // Encrypt this string before storing, as it contains sensitive data.
                asylumCase.write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, HandlerUtils.encrypt(homeOfficeAppellantsSerialised));
            } catch (Exception ex) {
                log.error("Could not serialise list of Home Office appellants: {}", ex.getMessage());
            }
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
