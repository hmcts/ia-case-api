package uk.gov.hmcts.reform.iacaseapi.forms.domain.entities;

import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

public class Event {

    private final EventId id;
    private final String description;
    private final String summary;

    public Event(
        EventId id
    ) {
        this(
            id,
            "",
            ""
        );
    }

    public Event(
        EventId id,
        String description,
        String summary
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (description == null) {
            throw new IllegalArgumentException("description cannot be null");
        }

        if (summary == null) {
            throw new IllegalArgumentException("summary cannot be null");
        }

        this.id = id;
        this.description = description;
        this.summary = summary;
    }

    public EventId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSummary() {
        return summary;
    }
}
