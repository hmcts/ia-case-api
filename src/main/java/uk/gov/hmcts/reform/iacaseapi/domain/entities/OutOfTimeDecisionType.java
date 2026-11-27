package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutOfTimeDecisionType {

    IN_TIME("inTime"),
    APPROVED("approved"),
    REJECTED("rejected");

    @JsonValue
    private final String value;

    @JsonCreator
    OutOfTimeDecisionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
