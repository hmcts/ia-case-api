package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class WitnessHandler {

    protected List<IdValue<WitnessDetails>> buildInclusiveWitnessDetailsList(AsylumCase asylumCase, AsylumCase oldAsylumCase) {
        Optional<List<IdValue<WitnessDetails>>> oldWitnessesCollectionOptional = oldAsylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> oldWitnessesCollection = oldWitnessesCollectionOptional.orElse(Collections.emptyList());

        Optional<List<IdValue<WitnessDetails>>> newWitnessesCollectionOptional = asylumCase.read(WITNESS_DETAILS);
        List<IdValue<WitnessDetails>> newWitnessesCollection = newWitnessesCollectionOptional.orElse(Collections.emptyList());
        List<String> newWitnessesNames = newWitnessesCollection.stream()
            .map(idValue -> idValue.getValue().buildWitnessFullName()).toList();
        List<String> newWitnessesPartyIds = newWitnessesCollection.stream()
            .map(idValue -> idValue.getValue().getWitnessPartyId()).toList();

        List<IdValue<WitnessDetails>> inclusiveWitnessCollection = new ArrayList<>();

        int deletedWitnesses = 0;

        int i = 0;
        while (i < oldWitnessesCollection.size()) {
            IdValue<WitnessDetails> idValueOldWitness = oldWitnessesCollection.get(i);
            String oldPartyId = idValueOldWitness.getValue().getWitnessPartyId();

            if (newWitnessesPartyIds.contains(oldPartyId)) {
                idValueOldWitness.getValue().setIsWitnessDeleted(NO);

                Optional<IdValue<WitnessDetails>> newWitnessOpt = newWitnessesCollection
                    .stream()
                    .filter(idValue -> Objects.equals(idValue.getValue().getWitnessPartyId(), oldPartyId))
                    .findFirst();

                newWitnessOpt.ifPresent(oldWitness -> {
                    idValueOldWitness.getValue().setWitnessName(oldWitness.getValue().getWitnessName());
                    idValueOldWitness.getValue().setWitnessFamilyName(oldWitness.getValue().getWitnessFamilyName());
                });

            } else {
                idValueOldWitness.getValue().setIsWitnessDeleted(YES);
                deletedWitnesses++;
            }
            inclusiveWitnessCollection.add(idValueOldWitness);
            i++;
        }
        i -= deletedWitnesses;
        while (i < newWitnessesCollection.size()) {
            newWitnessesCollection.get(i).getValue().setIsWitnessDeleted(NO);
            inclusiveWitnessCollection.add(newWitnessesCollection.get(i));
            i++;
        }

        return inclusiveWitnessCollection;
    }

    protected boolean isWitnessDeleted(IdValue<WitnessDetails> witnessDetailsIdValue) {
        return Objects.equals(witnessDetailsIdValue.getValue().getIsWitnessDeleted(), YES);
    }

    protected List<IdValue<WitnessDetails>> nonDeletedWitnesses(List<IdValue<WitnessDetails>> inclusiveWitnessList) {
        return inclusiveWitnessList.stream()
            .filter(idValue -> !Objects.equals(idValue.getValue().getIsWitnessDeleted(), YES))
            .toList();
    }

    protected List<IdValue<WitnessDetails>> deletedWitnesses(List<IdValue<WitnessDetails>> inclusiveWitnessList) {
        return inclusiveWitnessList.stream()
            .filter(idValue -> Objects.equals(idValue.getValue().getIsWitnessDeleted(), YES))
            .toList();
    }

    private void updateWitnessName() {

    }
}
