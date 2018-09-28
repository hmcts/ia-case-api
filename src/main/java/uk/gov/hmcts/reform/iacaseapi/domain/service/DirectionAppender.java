package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirection;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirections;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

@Service
public class DirectionAppender {

    public void append(
        AsylumCase asylumCase,
        Direction direction
    ) {
        List<IdValue<SentDirection>> allSentDirections = new ArrayList<>();

        allSentDirections.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                new SentDirection(
                    direction,
                    LocalDate.now().toString(),
                    "Outstanding"
                )
            )
        );

        SentDirections sentDirections =
            asylumCase
                .getSentDirections()
                .orElse(new SentDirections());

        if (sentDirections.getSentDirections().isPresent()) {
            allSentDirections.addAll(
                sentDirections.getSentDirections().get()
            );
        }

        sentDirections.setSentDirections(allSentDirections);

        asylumCase.setSentDirections(sentDirections);
    }
}
