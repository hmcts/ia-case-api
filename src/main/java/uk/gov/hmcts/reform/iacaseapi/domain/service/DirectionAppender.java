package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionAppender {

    public void append(
        AsylumCase asylumCase,
        Direction newDirection
    ) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        requireNonNull(newDirection, "newDirection must not be null");

        final List<IdValue<Direction>> existingDirections =
            asylumCase
                .getDirections()
                .orElse(Collections.emptyList());

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        int index = existingDirections.size() + 1;

        allDirections.add(new IdValue<>(String.valueOf(index--), newDirection));

        for (IdValue<Direction> existingDirection : existingDirections) {
            allDirections.add(new IdValue<>(String.valueOf(index--), existingDirection.getValue()));
        }

        asylumCase.setDirections(allDirections);
    }
}
