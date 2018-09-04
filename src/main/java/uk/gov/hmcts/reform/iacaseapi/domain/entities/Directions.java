package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;

public class Directions {

    private Optional<List<Direction>> directions = Optional.empty();

    private Directions() {
        // noop -- for deserializer
    }

    public Optional<List<Direction>> getDirections() {
        return directions;
    }

    public void setDirections(List<Direction> directions) {
        this.directions = Optional.ofNullable(directions);
    }
}
