package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirection;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

@Service
public class DeadlineDirectionExtractor {

    public Optional<SentDirection> extract(
        AsylumCase asylumCase
    ) {
        if (asylumCase
            .getSentDirections()
            .isPresent()) {

            if (asylumCase
                .getSentDirections()
                .get()
                .getSentDirections()
                .isPresent()) {

                return asylumCase
                    .getSentDirections()
                    .get()
                    .getSentDirections()
                    .get()
                    .stream()
                    .map(IdValue::getValue)
                    .filter(direction ->
                        direction
                            .getDirection()
                            .orElse("")
                            .toLowerCase()
                            .contains("deadline")
                    )
                    .findFirst();
            }
        }

        return Optional.empty();
    }
}
