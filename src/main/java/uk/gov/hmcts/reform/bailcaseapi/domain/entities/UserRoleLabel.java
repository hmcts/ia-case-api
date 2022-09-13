package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRoleLabel {

    JUDGE("Judge"),
    TRIBUNAL_CASEWORKER("Tribunal Caseworker"),
    ADMIN_OFFICER("Admin Officer"),
    HOME_OFFICE_BAIL("Home Office"),
    LEGAL_REPRESENTATIVE("Legal Representative"),
    SYSTEM("System");

    @JsonValue
    private final String id;

    UserRoleLabel(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
