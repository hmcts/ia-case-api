package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class Direction {

    private Optional<String> direction = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<String> parties = Optional.empty();

    private Direction() {
        // noop -- for deserializer
    }

    public Optional<String> getDirection() {
        return direction;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getParties() {
        return parties;
    }

    public void setDirection(String direction) {
        this.direction = Optional.ofNullable(direction);
    }

    public void setDescription(String description) {
        this.description = Optional.ofNullable(description);
    }

    public void setParties(String parties) {
        this.parties = Optional.ofNullable(parties);
    }
}
