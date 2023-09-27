package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;

import java.util.*;


public class WitnessCaseFlagsHandler extends AppellantCaseFlagsHandler {

    protected DateProvider systemDateProvider;

    protected List<CaseFlagDetail> getWitnessCaseFlags(Optional<List<PartyFlagIdValue>> optionalPartyFlags, String witnessId) {
        List<CaseFlagDetail> witnessCaseFlags = new ArrayList<>();
        if (optionalPartyFlags.isPresent()) {
            Optional<PartyFlagIdValue> witnessPartyValue = optionalPartyFlags.get()
                    .stream()
                    .filter(partyFlagIdValue -> partyFlagIdValue.getPartyId().equals(witnessId))
                    .findFirst();

            if (witnessPartyValue.isPresent()) {
                witnessCaseFlags = witnessPartyValue.get().getValue().getDetails();
            }
        }
        return witnessCaseFlags;
    }

}
