package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class WitnessesService {

    public static void appendWitnessPartyId(AsylumCase asylumCase) {

        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        AtomicInteger index = new AtomicInteger(1);
        List<IdValue<WitnessDetails>> newWitnessDetails =
            witnessDetailsOptional.orElse(emptyList())
                .stream()
                .map(idValue -> new IdValue<>(
                    String.valueOf(index.getAndIncrement()),
                    new WitnessDetails(
                        defaultIfNull(idValue.getValue().getWitnessPartyId(), HearingPartyIdGenerator.generate()),
                        idValue.getValue().getWitnessName(),
                        idValue.getValue().getWitnessFamilyName()
                    )
                ))
                .collect(toList());

        asylumCase.write(WITNESS_DETAILS, newWitnessDetails);
    }
}
