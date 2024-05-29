package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UpdateTribunalRules {

    UNDER_RULE_31("underRule31"),
    UNDER_RULE_32("underRule32");

    @JsonValue
    private final String value;

    UpdateTribunalRules(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}