package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Attributes {
    CASE_ID("caseId"),
    PRIMARY_LOCATION("primaryLocation"),
    JURISDICTION("jurisdiction"),
    REGION("region"),
    CASE_TYPE("caseType");

    @JsonValue
    private final String value;

    public String getValue() {
        return value;
    }

    Attributes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
