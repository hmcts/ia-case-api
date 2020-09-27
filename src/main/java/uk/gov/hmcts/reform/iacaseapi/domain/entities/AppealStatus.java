package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AppealStatus {

    REINSTATED("Reinstated");

    @JsonValue
    private String value;

    AppealStatus(String value) {
        this.value = value;
    }

    public static AppealStatus from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " not an AppealStatus"));
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
