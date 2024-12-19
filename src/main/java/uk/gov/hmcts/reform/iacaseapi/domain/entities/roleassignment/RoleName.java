package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleName {
    TRIBUNAL_CASEWORKER("tribunal-caseworker"),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker"),
    LEGAL_REPRESENTATIVE("[LEGALREPRESENTATIVE]"),
    CREATOR("[CREATOR]");

    @JsonValue
    private final String value;

    public String getValue() {
        return value;
    }

    RoleName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
