package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

@Service
public class DeadlineDirectionExtractor {

    public Optional<Direction> extract(
        AsylumCase asylumCase
    ) {
        if (asylumCase
            .getDirections()
            .isPresent()) {

            if (asylumCase
                .getDirections()
                .get()
                .getDirections()
                .isPresent()) {

                return asylumCase
                    .getDirections()
                    .get()
                    .getDirections()
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
