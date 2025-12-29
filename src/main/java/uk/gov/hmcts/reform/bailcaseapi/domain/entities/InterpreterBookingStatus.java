package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum InterpreterBookingStatus {
    NOT_REQUESTED("notRequested"),
    REQUESTED("requested"),
    BOOKED("booked"),
    CANCELLED("cancelled");

    @JsonValue
    private final String value;

    InterpreterBookingStatus(String value) {
        this.value = value;
    }

    public static Optional<InterpreterBookingStatus> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
