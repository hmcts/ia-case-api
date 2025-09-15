package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class RoleNameTest {

    @ParameterizedTest
    @EnumSource(value = RoleName.class)
    void to_string_gets_values(RoleName roleName) {
        assertEquals(roleName.toString(), roleName.getValue());
    }

    @ParameterizedTest
    @CsvSource({
        "case-manager, CASE_MANAGER",
        "tribunal-caseworker, TRIBUNAL_CASEWORKER",
        "challenged-access-legal-operations, CHALLENGED_ACCESS_LEGAL_OPERATIONS",
        "senior-tribunal-caseworker, SENIOR_TRIBUNAL_CASEWORKER",
        "hearing-centre-admin, HEARING_CENTRE_ADMIN",
        "ctsc, CTSC",
        "ctsc-team-leader, CTSC_TEAM_LEADER",
        "national-business-centre, NATIONAL_BUSINESS_CENTRE",
        "challenged-access-ctsc, CHALLENGED_ACCESS_CTSC",
        "challenged-access-admin, CHALLENGED_ACCESS_ADMIN",
        "judge, JUDGE",
        "senior-judge, SENIOR_JUDGE",
        "leadership-judge, LEADERSHIP_JUDGE",
        "fee-paid-judge, FEE_PAID_JUDGE",
        "lead-judge, LEAD_JUDGE",
        "hearing-judge, HEARING_JUDGE",
        "ftpa-judge, FTPA_JUDGE",
        "hearing-panel-judge, HEARING_PANEL_JUDGE",
        "challenged-access-judiciary, CHALLENGED_ACCESS_JUDICIARY",
        "[LEGALREPRESENTATIVE], LEGAL_REPRESENTATIVE",
        "[CREATOR], CREATOR",
        "unknown, UNKNOWN"
    })
    void has_correct_values(String expectedValue, RoleName roleName) {
        assertEquals(expectedValue, roleName.getValue());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(22, RoleName.values().length);
    }
}
