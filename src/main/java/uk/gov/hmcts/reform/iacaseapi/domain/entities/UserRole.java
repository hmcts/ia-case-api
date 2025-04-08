package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {

    CASE_OFFICER("caseworker-ia-caseofficer"),
    ADMIN_OFFICER("caseworker-ia-admofficer"),
    LEGAL_REPRESENTATIVE("caseworker-ia-legalrep-solicitor"),
    JUDICIARY("caseworker-ia-judiciary"),
    JUDGE("caseworker-ia-iacjudge"),
    SYSTEM("caseworker-ia-system"),
    HOME_OFFICE_APC("caseworker-ia-homeofficeapc"),
    HOME_OFFICE_LART("caseworker-ia-homeofficelart"),
    HOME_OFFICE_POU("caseworker-ia-homeofficepou"),
    HOME_OFFICE_GENERIC("caseworker-ia-respondentofficer"),
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
