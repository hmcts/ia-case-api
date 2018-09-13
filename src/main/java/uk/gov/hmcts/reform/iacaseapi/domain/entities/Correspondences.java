package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class Correspondences {

    private Optional<List<IdValue<Correspondence>>> correspondences = Optional.empty();

    public Optional<List<IdValue<Correspondence>>> getCorrespondences() {
        return correspondences;
    }

    public void setCorrespondences(List<IdValue<Correspondence>> correspondences) {
        this.correspondences = Optional.ofNullable(correspondences);
    }
}
