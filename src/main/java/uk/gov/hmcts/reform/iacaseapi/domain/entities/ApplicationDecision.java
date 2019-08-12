package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationDecision {

    GRANTED("Granted"),
    REFUSED("Refused");

    @JsonValue
    private final String value;

    ApplicationDecision(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
