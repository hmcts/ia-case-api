package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

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

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
