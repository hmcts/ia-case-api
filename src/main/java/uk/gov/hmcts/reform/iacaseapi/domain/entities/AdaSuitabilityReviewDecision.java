package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AdaSuitabilityReviewDecision {

    SUITABLE("suitable"),
    UNSUITABLE("unsuitable");

    @JsonValue
    private final String value;

    AdaSuitabilityReviewDecision(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}