package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CostsDecisionType {
    WITHOUT_AN_ORAL_HEARING("Without an oral hearing"),
    WITH_AN_ORAL_HEARING("With an oral hearing");

    @JsonValue
    private String value;

    CostsDecisionType(String value) {
        this.value = value;
    }

    public static Optional<CostsDecisionType> from(
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
