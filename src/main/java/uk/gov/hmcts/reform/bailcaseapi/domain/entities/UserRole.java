package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {

    CASE_OFFICER("caseworker-ia-caseofficer"),
    ADMIN_OFFICER("caseworker-ia-admofficer"),
    LEGAL_REPRESENTATIVE("caseworker-ia-legalrep-solicitor"),
    JUDICIARY("caseworker-ia-judiciary"),
    JUDGE("caseworker-ia-iacjudge"),
    SYSTEM("caseworker-ia-system"),
    HOME_OFFICE_BAIL("caseworker-ia-homeofficebail"),
    HOME_OFFICE_POU("caseworker-ia-homeofficepou"),
    CITIZEN("citizen");

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
