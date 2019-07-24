package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {

    CASE_OFFICER("caseworker-ia-caseofficer"),
    ADMIN_OFFICER("caseworker-ia-admofficer"),
    LEGAL_REPRESENTATIVE("caseworker-ia-legalrep-solicitor"),
    JUDICIARY("caseworker-ia-judiciary"),
    SYSTEM("caseworker-ia-system");

    @JsonValue
    private final String id;

    UserRole(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
