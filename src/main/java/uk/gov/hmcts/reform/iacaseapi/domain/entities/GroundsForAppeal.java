package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class GroundsForAppeal {

    private Optional<List<IdValue<GroundForAppeal>>> groundsForAppeal = Optional.empty();

    public GroundsForAppeal() {
        // noop
    }

    public GroundsForAppeal(List<IdValue<GroundForAppeal>> groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }

    public Optional<List<IdValue<GroundForAppeal>>> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public void setGroundsForAppeal(List<IdValue<GroundForAppeal>> groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }
}
