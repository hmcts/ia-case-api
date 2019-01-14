package uk.gov.hmcts.reform.iacaseapi.domain.entities;

public enum UserRole {

    CASE_OFFICER("caseworker-ia-caseofficer"),
    LEGAL_REPRESENTATIVE("caseworker-ia-legalrep-solicitor"),
    JUDICIARY("caseworker-ia-judiciary"),
    SYSTEM("caseworker-ia-system");

    private final String id;

    UserRole(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
