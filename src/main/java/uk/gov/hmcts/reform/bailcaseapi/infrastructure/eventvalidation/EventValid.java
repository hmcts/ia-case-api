package uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation;

import java.util.Objects;

public class EventValid {
    public static final EventValid VALID_EVENT = new EventValid();

    private final String invalidReason;

    public EventValid() {
        this(null);
    }

    public EventValid(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public boolean isValid() {
        return invalidReason == null;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventValid that = (EventValid) o;
        return Objects.equals(invalidReason, that.invalidReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invalidReason);
    }
}

