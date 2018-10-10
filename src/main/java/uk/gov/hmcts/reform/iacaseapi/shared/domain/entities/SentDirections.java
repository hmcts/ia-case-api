package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

public class SentDirections {

    private Optional<List<IdValue<SentDirection>>> sentDirections = Optional.empty();

    public Optional<List<IdValue<SentDirection>>> getSentDirections() {
        return sentDirections;
    }

    public void setSentDirections(List<IdValue<SentDirection>> sentDirections) {
        this.sentDirections = Optional.ofNullable(sentDirections);
    }
}
