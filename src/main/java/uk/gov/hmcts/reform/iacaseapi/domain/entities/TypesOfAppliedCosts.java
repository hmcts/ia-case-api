package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypesOfAppliedCosts {
    UNREASONABLE_COSTS("Unreasonable costs"),
    WASTED_COSTS("Wasted costs");

    @JsonValue
    private final String value;

    TypesOfAppliedCosts(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
