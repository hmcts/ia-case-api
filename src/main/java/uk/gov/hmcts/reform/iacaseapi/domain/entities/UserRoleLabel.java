package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRoleLabel {

    JUDGE("Judge"),
    TRIBUNAL_CASEWORKER("Tribunal Caseworker"),
    ADMIN_OFFICER("Admin Officer"),
    HOME_OFFICE_GENERIC("Respondent"),
    LEGAL_REPRESENTATIVE("Legal representative"),
    SYSTEM("System"),
    TASK_RETRIGGER("Task retrigger"),
    CITIZEN("Appellant");

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
