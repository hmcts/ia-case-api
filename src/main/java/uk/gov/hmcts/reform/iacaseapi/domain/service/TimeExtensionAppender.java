package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class TimeExtensionAppender {

    private final DateProvider dateProvider;

    public TimeExtensionAppender(
        DateProvider dateProvider
    ) {
        this.dateProvider = dateProvider;
    }

    public List<IdValue<TimeExtension>> append(
        List<IdValue<TimeExtension>> existingTimeExtensions,
        State state,
        String reason,
        List<IdValue<Document>> evidence
    ) {
        requireNonNull(existingTimeExtensions, "existingTimeExtension must not be null");
        requireNonNull(state, "state must not be null");
        requireNonNull(reason, "reason must not be null");

        final TimeExtension newTimeExtension = new TimeExtension(
            dateProvider.now().toString(),
            reason,
            state,
            TimeExtensionStatus.SUBMITTED,
            evidence
        );

        final List<IdValue<TimeExtension>> allTimeExtension = new ArrayList<>();

        int index = existingTimeExtensions.size() + 1;

        allTimeExtension.add(new IdValue<>(String.valueOf(index--), newTimeExtension));

        for (IdValue<TimeExtension> existingTimeExtension : existingTimeExtensions) {
            allTimeExtension.add(new IdValue<>(String.valueOf(index--), existingTimeExtension.getValue()));
        }

        return allTimeExtension;
    }
}
