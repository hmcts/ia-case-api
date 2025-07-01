package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RoleName {
    CASE_MANAGER("case-manager"),
    // caseworker-ia-caseofficer
    TRIBUNAL_CASEWORKER("tribunal-caseworker"),
    CHALLENGED_ACCESS_LEGAL_OPERATIONS("challenged-access-legal-operations"),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker"),
    // caseworker-ia-admofficer
    HEARING_CENTRE_ADMIN("hearing-centre-admin"),
    CTSC("ctsc"),
    CTSC_TEAM_LEADER("ctsc-team-leader"),
    NATIONAL_BUSINESS_CENTRE("national-business-centre"),
    CHALLENGED_ACCESS_CTSC("challenged-access-ctsc"),
    CHALLENGED_ACCESS_ADMIN("challenged-access-admin"),
    // caseworker-ia-iacjudge
    JUDGE("judge"),
    SENIOR_JUDGE("senior-judge"),
    LEADERSHIP_JUDGE("leadership-judge"),
    FEE_PAID_JUDGE("fee-paid-judge"),
    LEAD_JUDGE("lead-judge"),
    HEARING_JUDGE("hearing-judge"),
    FTPA_JUDGE("ftpa-judge"),
    HEARING_PANEL_JUDGE("hearing-panel-judge"),
    CHALLENGED_ACCESS_JUDICIARY("challenged-access-judiciary"),
    // caseworker-ia-legalrep-solicitor
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
