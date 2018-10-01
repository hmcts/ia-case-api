package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Collections;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirection;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirections;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

@Service
public class SentDirectionCompleter {

    public void tryMarkAsComplete(
        AsylumCase asylumCase,
        String direction
    ) {
        SentDirections sentDirections =
            asylumCase
                .getSentDirections()
                .orElse(new SentDirections());

        sentDirections
            .getSentDirections()
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(sentDirection ->
                sentDirection
                    .getDirection()
                    .orElse("")
                    .equalsIgnoreCase(direction)
            )
            .forEach(SentDirection::markAsComplete);
    }
}
