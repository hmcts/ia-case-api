package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class Directions {

    private Optional<List<IdValue<Direction>>> directions = Optional.empty();

    public Optional<List<IdValue<Direction>>> getDirections() {
        return directions;
    }

    public void setDirections(List<IdValue<Direction>> directions) {
        this.directions = Optional.ofNullable(directions);
    }
}
