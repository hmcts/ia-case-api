package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class WitnessNamesUpdateService {

    private WitnessNamesUpdateService() {
    }

    public static void update(AsylumCase asylumCase) {
        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        witnessDetailsOptional.ifPresent(idValues -> {
            idValues.forEach(idValue -> {
                WitnessDetails witnessDetails = idValue.getValue();
                if (witnessDetails.getWitnessFamilyName() == null
                    || witnessDetails.getWitnessFamilyName().isBlank()) {

                    update(witnessDetails);
                }
            });
            asylumCase.write(WITNESS_DETAILS, idValues);
        });
    }

    private static void update(WitnessDetails witnessDetails) {
        String witnessName = witnessDetails.getWitnessName();
        if (!(witnessName == null || witnessName.isEmpty())) {
            if (witnessName.isBlank()) {
                witnessDetails.setWitnessFamilyName(witnessName);
            } else {
                String[] fullName = witnessName.split(" ", 2);
                witnessDetails.setWitnessName(fullName[0]);
                witnessDetails.setWitnessFamilyName(fullName.length > 1 ? fullName[1] : " ");
            }
        }
    }
}
