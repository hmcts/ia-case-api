package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;

@Service
public class DeadlineDirectionDueDateExtractor {

    private final DeadlineDirectionExtractor deadlineDirectionExtractor;

    public DeadlineDirectionDueDateExtractor(
        @Autowired DeadlineDirectionExtractor deadlineDirectionExtractor
    ) {
        this.deadlineDirectionExtractor = deadlineDirectionExtractor;
    }

    public Optional<String> extract(
        AsylumCase asylumCase
    ) {
        Optional<Direction> deadlineDirection =
            deadlineDirectionExtractor
                .extract(asylumCase);

        if (!deadlineDirection.isPresent()) {
            return Optional.empty();
        }

        return deadlineDirection
            .get()
            .getDueDate();
    }
}
