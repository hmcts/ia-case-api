package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValueMixin;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.home-office-validation.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class HomeOfficeReferenceHandlerOnSubmit implements PreSubmitCallbackHandler<AsylumCase> {

    public HomeOfficeReferenceHandlerOnSubmit() {
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && List.of(Event.START_APPEAL, Event.EDIT_APPEAL, Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellantsOpt = asylumCase.read(HOME_OFFICE_APPELLANTS);
        List<IdValue<HomeOfficeAppellant>> homeOfficeAppellants = homeOfficeAppellantsOpt.orElse(emptyList());
        String homeOfficeAppellantsSerialised = asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class).orElse("");
        // If the array of Home Office appellants does not exist but the serialised version does, deserialise it now
        if (homeOfficeAppellants.isEmpty() && !homeOfficeAppellantsSerialised.isEmpty()) {
            // Retrieve the UAN or GWF from the case record
            String homeOfficeReferenceNumber = asylumCase
                    .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                    .orElse("");
            if (homeOfficeReferenceNumber.isEmpty()) {
                homeOfficeReferenceNumber = asylumCase
                            .read(GWF_REFERENCE_NUMBER, String.class)
                            .orElseThrow(() -> new IllegalStateException(
                                "homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed"));
            }
            log.info("Writing previously retrieved Home Office appellant data to the case record in full for case with Home Office reference {}.", homeOfficeReferenceNumber);
            // We need the mapper and mix-in to overcome a CCD bug concerning collections during the mid-event (see comments below).
            ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(IdValue.class, IdValueMixin.class); 
            try {
                homeOfficeAppellants = mapper.readValue(
                                                            homeOfficeAppellantsSerialised,
                                                            new TypeReference<List<IdValue<HomeOfficeAppellant>>>() {}
                                                       );
                asylumCase.write(HOME_OFFICE_APPELLANTS, homeOfficeAppellants); // this will now work because we are no longer in the mid-event
            } catch (Exception ex) {
                log.error("Could not deserialise list of Home Office appellants from serialised string {} for case with Home Office reference {}:\n\n{}",
                          homeOfficeAppellantsSerialised, homeOfficeReferenceNumber, ex.getMessage());
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}