package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingPartyIdGenerator;

@Component
public class WitnessesPartyIdAppender implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = Arrays.asList(
                Event.DRAFT_HEARING_REQUIREMENTS, Event.UPDATE_HEARING_REQUIREMENTS);

        return (callbackStage == MID_EVENT
            && IS_WITNESSES_ATTENDING_PAGE_ID.equals(callback.getPageId())
            && targetEvents.contains(callback.getEvent()));
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        AtomicInteger index = new AtomicInteger(1);
        List<IdValue<WitnessDetails>> newWitnessDetails =
            witnessDetailsOptional.orElse(emptyList())
                .stream()
                .map(idValue -> {
                    return new IdValue<>(
                        String.valueOf(index.getAndIncrement()),
                        new WitnessDetails(
                            defaultIfNull(idValue.getValue().getWitnessPartyId(), HearingPartyIdGenerator.generate()),
                            idValue.getValue().getWitnessName(),
                            idValue.getValue().getWitnessFamilyName()
                        )
                    );
                })
                .collect(toList());


        asylumCase.write(WITNESS_DETAILS, newWitnessDetails);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
