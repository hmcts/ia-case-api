package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AppealDecision {

    ALLOWED("allowed"),
    DISMISSED("dismissed");

    @JsonValue
    private String value;

    AppealDecision(String value) {
        this.value = value;
    }

    public static AppealDecision from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " not an AppealDecision"));
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
