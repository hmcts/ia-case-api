package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class SentDirection extends Direction {

    private Optional<String> status = Optional.empty();

    private SentDirection() {
        // noop -- for deserializer
    }

    public SentDirection(
        Direction direction,
        String status
    ) {
        this.setDirection(direction.getDirection().orElse(null));
        this.setDescription(direction.getDescription().orElse(null));
        this.setParties(direction.getParties().orElse(null));
        this.setDueDate(direction.getDueDate().orElse(null));
        this.status = Optional.of(status);
    }

    public Optional<String> getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = Optional.ofNullable(status);
    }
}
