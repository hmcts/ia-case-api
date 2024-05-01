package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CostsDecision {
    ORDER_MADE("Order made"),
    ORDER_NOT_MADE("Order not made"),
    PENDING("Pending");

    @JsonValue
    private String value;

    CostsDecision(String value) {
        this.value = value;
    }

    public static Optional<CostsDecision> from(
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
