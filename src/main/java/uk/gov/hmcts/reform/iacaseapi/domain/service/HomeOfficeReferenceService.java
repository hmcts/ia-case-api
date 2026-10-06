package uk.gov.hmcts.reform.iacaseapi.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValueMixin;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    private final String homeOfficeSerialisedEncryptionKey;

    public HomeOfficeReferenceService(HomeOfficeApi<AsylumCase> homeOfficeApi,
                                      @Value("${homeOfficeApi.serialisation.encryption.key}") String homeOfficeSerialisedEncryptionKey) {
        this.homeOfficeApi = homeOfficeApi;
        this.homeOfficeSerialisedEncryptionKey = homeOfficeSerialisedEncryptionKey;
    }

    // Note: don't cache this response, as we want to get fresh data each time in case something changes at the Home Office's end.
    public List<IdValue<HomeOfficeAppellant>> getHomeOfficeReferenceData(String hoReference, Callback<AsylumCase> callback) {
        // We need the mapper and mix-in to overcome a CCD bug concerning collections during the mid-event (see comments below).
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(IdValue.class, IdValueMixin.class);
        // Check case for existing data.
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<String> homeOfficeAppellantsSerialisedEncrypted = asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class);
        List<IdValue<HomeOfficeAppellant>> homeOfficeAppellants = emptyList();
        // If we have a list of appellants already (in serialised form - see comments below), don't call the API again.
        if (homeOfficeAppellantsSerialisedEncrypted.isPresent()) {
            log.info("Deserialising and returning previously retrieved Home Office appellant data for case with Home Office reference {}.", hoReference);
            homeOfficeAppellants = deserialiseHomeOfficeAppellantList(homeOfficeAppellantsSerialisedEncrypted.get(),
                homeOfficeSerialisedEncryptionKey, asylumCase, hoReference);
            if (!homeOfficeAppellants.isEmpty()) {
                return homeOfficeAppellants;
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
                asylumCase.write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, HandlerUtils.encrypt(homeOfficeAppellantsSerialised, homeOfficeSerialisedEncryptionKey));
            } catch (Exception ex) {
                log.error("Could not serialise list of Home Office appellants: {}", ex.getMessage());
            }
            asylumCase.write(HOME_OFFICE_APPELLANT_CLAIM_DATE, asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_CLAIM_DATE, String.class).orElse(null));
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_DATE, asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_DECISION_DATE, String.class).orElse(null));
            asylumCase.write(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, asylumCaseWithHomeOfficeData.read(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE, String.class).orElse(null));
        } else {
            // The API did not return any data; log the diagnostic message appropriately
            String logMessage = buildLogMessage(hoReference, responseStatus);
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

    public String buildLogMessage(String hoReference, HomeOfficeApiResponseStatusType responseStatus) {
        return "Biographic information from Home Office asylum (etc.) application with Home Office reference "
            + hoReference + " could not be retrieved.\n\n" + responseStatus.getHoIntegrationErrorText(hoReference)
            + "\n\nSee the corresponding logs in ia-home-office-integration-api for more details.";
    }

    public static List<IdValue<HomeOfficeAppellant>> deserialiseHomeOfficeAppellantList(String homeOfficeAppellantsSerialisedEncrypted,
                                                                                        String homeOfficeSerialisedEncryptionKey,
                                                                                        AsylumCase asylumCase,
                                                                                        String homeOfficeReferenceNumber) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(IdValue.class, IdValueMixin.class);
        try {
            String homeOfficeAppellantsSerialised = HandlerUtils.decrypt(homeOfficeAppellantsSerialisedEncrypted, homeOfficeSerialisedEncryptionKey);
            return mapper.readValue(
                homeOfficeAppellantsSerialised,
                new TypeReference<List<IdValue<HomeOfficeAppellant>>>() {
                }
            );
        } catch (Exception ex) {
            log.error("Could not deserialise list of Home Office appellants from encrypted serialised string {} for case with Home Office reference {}:\n\n{}",
                homeOfficeAppellantsSerialisedEncrypted, homeOfficeReferenceNumber, ex.getMessage());
        }
        return emptyList();
    }

    public static void writeHomeOfficeAppellants(AsylumCase asylumCase, List<IdValue<HomeOfficeAppellant>> homeOfficeAppellants) {
        if (!homeOfficeAppellants.isEmpty()) {
            asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants); // this will now work because we are no longer in the mid-event
            String ppNumber = HandlerUtils.getPpNumberFromHomeOfficeAppellants(asylumCase);
            asylumCase.write(HOME_OFFICE_APPELLANTS_PP_NUMBER, ppNumber);
            asylumCase.clear(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY);
            asylumCase.write(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.YES);

            homeOfficeAppellants.stream()
                .map(IdValue::getValue)
                .filter(a -> ppNumber != null && ppNumber.equals(a.getPp()))
                .findFirst()
                .ifPresent(a -> {
                    asylumCase.write(HO_RIGHT_OF_APPEAL, a.getRoa());
                    asylumCase.write(HO_ASYLUM_SUPPORT, a.getAsylumSupport());
                    asylumCase.write(HO_FEE_WAIVER, a.getHoFeeWaiver());
                    // need to map language to ccd field as the Home Office API returns some language code
                    asylumCase.write(HOME_OFFICE_APPELLANT_LANGUAGE, a.getLanguage());
                    asylumCase.write(HO_INTERPRETER_REQUIRED, a.getInterpreterNeeded());
                });
        }
    }
}
