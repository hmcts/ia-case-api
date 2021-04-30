package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class DirectionAppender {

    private final DateProvider dateProvider;
    private final WaFieldsPublisher waFieldsPublisher;

    public DirectionAppender(
            DateProvider dateProvider,
            WaFieldsPublisher waFieldsPublisher) {
        this.dateProvider = dateProvider;
        this.waFieldsPublisher = waFieldsPublisher;
    }

    public List<IdValue<Direction>> append(
        AsylumCase asylumCase,
        List<IdValue<Direction>> existingDirections,
        String explanation,
        Parties parties,
        String dateDue,
        DirectionTag tag
    ) {
        return append(asylumCase, existingDirections, explanation, parties, dateDue, tag, Collections.emptyList());
    }

    public List<IdValue<Direction>> append(
            AsylumCase asylumCase,
            List<IdValue<Direction>> existingDirections,
            String explanation,
            Parties parties,
            String dateDue,
            DirectionTag tag,
            List<IdValue<ClarifyingQuestion>> questions
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
                Collections.emptyList(),
                questions
        );

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        int index = existingDirections.size() + 1;

        allDirections.add(new IdValue<>(String.valueOf(index--), newDirection));

        for (IdValue<Direction> existingDirection : existingDirections) {
            allDirections.add(new IdValue<>(String.valueOf(index--), existingDirection.getValue()));
        }

        waFieldsPublisher.addLastModifiedDirection(asylumCase, explanation, parties, dateDue, tag);

        return allDirections;
    }
}
