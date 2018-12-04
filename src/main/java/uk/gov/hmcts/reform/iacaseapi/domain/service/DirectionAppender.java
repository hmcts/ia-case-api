package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
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
        String newDirectionExplanation,
        Parties newDirectionParties,
        String newDirectionDateDue
    ) {
        requireNonNull(existingDirections, "existingDirections must not be null");
        requireNonNull(newDirectionExplanation, "newDirectionExplanation must not be null");
        requireNonNull(newDirectionParties, "newDirectionParties must not be null");
        requireNonNull(newDirectionDateDue, "newDirectionDateDue must not be null");

        final Direction newDirection = new Direction(
            newDirectionExplanation,
            newDirectionParties,
            newDirectionDateDue,
            dateProvider.now().toString()
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
