package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class CaseSummary {

    private Optional<GroundsForAppeal> groundsForAppeal = Optional.empty();

    public Optional<GroundsForAppeal> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public void setGroundsForAppeal(GroundsForAppeal groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }
}
