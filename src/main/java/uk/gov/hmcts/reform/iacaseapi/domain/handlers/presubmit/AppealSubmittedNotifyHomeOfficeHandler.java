package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValueMixin;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.validateAllDetails;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.home-office-validation.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AppealSubmittedNotifyHomeOfficeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String SUPPRESSION_LOG_FIELDS_NEW = "event: {}, "
                                                         + "CCD case ID: {}, "
                                                         + "HMCTS appeal ref: {}, "
                                                         + "Home Office reference no: {}, "
                                                         + "Home Office API response code: {}";

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;
    private final HomeOfficeReferenceService homeOfficeReferenceService;
    private final String homeOfficeSerialisedEncryptionKey;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST; // this handler MUST run after  AppealReferenceNumberHandler
    }

    public AppealSubmittedNotifyHomeOfficeHandler(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled,
        HomeOfficeReferenceService homeOfficeReferenceService,
        HomeOfficeApi<AsylumCase> homeOfficeApi,
        @Value("${homeOfficeApi.serialisation.encryption.key}")
        String homeOfficeSerialisedEncryptionKey) {
        this.homeOfficeApi = homeOfficeApi;
        this.homeOfficeReferenceService = homeOfficeReferenceService;
        this.homeOfficeSerialisedEncryptionKey = homeOfficeSerialisedEncryptionKey;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               // This handler must run once and only once for each appeal, ideally as soon as the appeal is first created (and no longer in DRAFT state)
               && (callback.getEvent() == SUBMIT_APPEAL);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        // Retrieve the UAN or GWF from the case record
        final String homeOfficeReferenceNumber = HandlerUtils.getUanOrGwf(asylumCase);
        if (homeOfficeReferenceNumber.isEmpty()) {
            throw new IllegalStateException("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed");
        }
        // Ensure this is present before calling the Home Office API (where it will be needed)
        final String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));
        // Re-validate the appeal with the Home Office API (in case anything has changed since the last time it was called)
        asylumCase.clear(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY);
        PreSubmitCallbackResponse<AsylumCase> validationResponse =
            validateAllDetails(callback, asylumCase, homeOfficeReferenceNumber, homeOfficeReferenceService);
        if (!validationResponse.getErrors().isEmpty()) {
            return validationResponse;
        }

        // For draft appeals created before the new Home Office API was implemented
        // we need to deserialise the list of newly validated Home Office appellants from the serialised string and
        // write it back to the case record
        if (asylumCase.read(HOME_OFFICE_APPELLANTS, List.class).isEmpty()) {
            // We need the mapper and mix-in to overcome a CCD bug concerning collections during the mid-event (see comments below).
            ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(IdValue.class, IdValueMixin.class);
            String encodedStr = asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class)
                .orElse("");
            try {
                String homeOfficeAppellantsSerialised = HandlerUtils
                    .decrypt(encodedStr, homeOfficeSerialisedEncryptionKey);
                List<IdValue<HomeOfficeAppellant>> homeOfficeAppellants = mapper.readValue(
                    homeOfficeAppellantsSerialised,
                    new TypeReference<>() {
                    }
                );
                asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants);
            } catch (Exception ex) {
                log.error("Could not deserialise list of Home Office appellants from encrypted serialised string {} for case with Home Office reference {}:\n\n{}",
                    encodedStr, homeOfficeReferenceNumber, ex.getMessage());
            }
        }

        // Details for logging purposes only
        final HomeOfficeApiResponseStatusType homeOfficeAppellantApiResponseStatus = asylumCase.read(
                            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                            .orElse(HomeOfficeApiResponseStatusType.UNKNOWN);
        final long caseId = callback.getCaseDetails().getId();

        log.info("Start: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS_NEW,
            callback.getEvent(), caseId, appealReferenceNumber, homeOfficeReferenceNumber, homeOfficeAppellantApiResponseStatus.getStatusCode());

        asylumCase = homeOfficeApi.aboutToSubmit(callback);

        log.info("Finish: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS_NEW,
            callback.getEvent(), caseId, appealReferenceNumber, homeOfficeReferenceNumber, homeOfficeAppellantApiResponseStatus.getStatusCode());

        asylumCase.clear(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY);
        asylumCase.write(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
