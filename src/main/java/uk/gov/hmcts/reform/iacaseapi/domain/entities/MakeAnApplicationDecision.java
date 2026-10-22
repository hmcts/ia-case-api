package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum  MakeAnApplicationDecision {

    GRANTED("Granted"),
    REFUSED("Refused");

    @JsonValue
    private final String value;

    @JsonCreator
    MakeAnApplicationDecision(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
