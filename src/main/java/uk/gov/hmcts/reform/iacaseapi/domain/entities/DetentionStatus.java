package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DetentionStatus {

    DETAINED("detained"),
    ACCELERATED("detainedAccelerated");

    @JsonValue
    private String value;

    DetentionStatus(String value) {
        this.value = value;
    }

    public static DetentionStatus from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " not a Detention Status"));
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
