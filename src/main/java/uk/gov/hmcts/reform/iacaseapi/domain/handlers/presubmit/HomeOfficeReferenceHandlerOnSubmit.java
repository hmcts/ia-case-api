package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.shouldValidateEditPersonalData;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService.deserialiseHomeOfficeAppellantList;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService.writeHomeOfficeAppellants;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.home-office-validation.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnProperty(
    name = "app.home-office-mock-turn-off-for-test.enabled",
    havingValue = "false"
)
public class HomeOfficeReferenceHandlerOnSubmit implements PreSubmitCallbackHandler<AsylumCase> {

    private final String homeOfficeSerialisedEncryptionKey;

    public HomeOfficeReferenceHandlerOnSubmit(@Value("${homeOfficeApi.serialisation.encryption.key}")
                                              String homeOfficeSerialisedEncryptionKey) {
        this.homeOfficeSerialisedEncryptionKey = homeOfficeSerialisedEncryptionKey;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (Set.of(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callback.getEvent())
            || shouldValidateEditPersonalData(callback));

    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String encodedStr = asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class).orElse("");
        if (!encodedStr.isEmpty()) {
            // Retrieve the UAN or GWF from the case record
            String homeOfficeReferenceNumber = HandlerUtils.getUanOrGwf(asylumCase);
            if (homeOfficeReferenceNumber.isBlank()) {
                throw new IllegalStateException("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed");
            }

            log.info("Writing retrieved Home Office appellant data to the case record in full for case with Home Office reference {}.", homeOfficeReferenceNumber);
            // We need the mapper and mix-in to overcome a CCD bug concerning collections during the mid-event (see comments below).
            List<IdValue<HomeOfficeAppellant>> homeOfficeAppellants = deserialiseHomeOfficeAppellantList(encodedStr,
                homeOfficeSerialisedEncryptionKey, asylumCase, homeOfficeReferenceNumber);
            writeHomeOfficeAppellants(asylumCase, homeOfficeAppellants);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}