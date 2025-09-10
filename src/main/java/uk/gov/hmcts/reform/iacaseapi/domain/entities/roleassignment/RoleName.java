package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RoleName {
    //Judicial Roles
    HEARING_JUDGE("hearing-judge"),
    LEAD_JUDGE("lead-judge"),
    FTPA_JUDGE("ftpa-judge"),
    HEARING_PANEL_JUDGE("hearing-panel-judge"),
    //Legal Ops Roles
    CASE_MANAGER("case-manager"),
    //Legacy Roles?
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
