package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionAppender {

    private final DateProvider dateProvider;

    public DirectionAppender(
        DateProvider dateProvider
    ) {
        this.dateProvider = dateProvider;
    }

    public List<IdValue<Direction>> append(
        List<IdValue<Direction>> existingDirections,
        String explanation,
        Parties parties,
        String dateDue,
        DirectionTag tag
    ) {
        requireNonNull(existingDirections, "existingDirections must not be null");
        requireNonNull(explanation, "explanation must not be null");
        requireNonNull(parties, "parties must not be null");
        requireNonNull(dateDue, "dateDue must not be null");
        requireNonNull(tag, "tag must not be null");

        final Direction newDirection = new Direction(
            explanation,
            parties,
            dateDue,
            dateProvider.now().toString(),
            tag,
            Collections.emptyList()
        );

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        int index = existingDirections.size() + 1;

        allDirections.add(new IdValue<>(String.valueOf(index--), newDirection));

        for (IdValue<Direction> existingDirection : existingDirections) {
            allDirections.add(new IdValue<>(String.valueOf(index--), existingDirection.getValue()));
        }

        return allDirections;
    }
}
