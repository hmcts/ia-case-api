package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RemissionDecision {

    APPROVED("approved"),
    PARTIALLY_APPROVED("partiallyApproved"),
    REJECTED("rejected");

    @JsonValue
    private final String id;

    RemissionDecision(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
