package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RoleName {
    CASE_MANAGER("case-manager"),
    TRIBUNAL_CASEWORKER("tribunal-caseworker"),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker"),
    LEGAL_REPRESENTATIVE("[LEGALREPRESENTATIVE]"),
    CREATOR("[CREATOR]");

    @JsonValue
    private final String value;

    RoleName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
