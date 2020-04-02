package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TimeExtensionDecision {
    GRANTED("granted"),
    REFUSED("refused");

    @JsonValue
    private final String id;

    TimeExtensionDecision(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
