package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

import static java.util.Arrays.stream;

public enum HearingType {

    HEARING("hearing"),
    APPOINTMENT("appointment");

    @JsonValue
    private String value;

    HearingType(String value) {
        this.value = value;
    }

    public static Optional<HearingType> from(
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
        return value;
    }
}
