package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

public class GroundsOfAppeal {

    private Optional<List<IdValue<GroundOfAppeal>>> groundsOfAppeal = Optional.empty();

    public GroundsOfAppeal() {
        // noop
    }

    public GroundsOfAppeal(List<IdValue<GroundOfAppeal>> groundsOfAppeal) {
        this.groundsOfAppeal = Optional.ofNullable(groundsOfAppeal);
    }

    public Optional<List<IdValue<GroundOfAppeal>>> getGroundsOfAppeal() {
        return groundsOfAppeal;
    }

    public void setGroundsOfAppeal(List<IdValue<GroundOfAppeal>> groundsOfAppeal) {
        this.groundsOfAppeal = Optional.ofNullable(groundsOfAppeal);
    }
}
