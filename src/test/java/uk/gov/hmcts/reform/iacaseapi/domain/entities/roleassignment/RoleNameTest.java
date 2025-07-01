package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RoleNameTest {

    @ParameterizedTest
    @EnumSource(value = RoleName.class)
    void to_string_gets_values(RoleName roleName) {
        assertEquals(roleName.toString(), roleName.getValue());
    }

    @Test
    void has_correct_values() {
        assertEquals("case-manager", RoleName.CASE_MANAGER.getValue());
        assertEquals("tribunal-caseworker", RoleName.TRIBUNAL_CASEWORKER.getValue());
        assertEquals("challenged-access-legal-operations", RoleName.CHALLENGED_ACCESS_LEGAL_OPERATIONS.getValue());
        assertEquals("senior-tribunal-caseworker", RoleName.SENIOR_TRIBUNAL_CASEWORKER.getValue());
        assertEquals("hearing-centre-admin", RoleName.HEARING_CENTRE_ADMIN.getValue());
        assertEquals("ctsc", RoleName.CTSC.getValue());
        assertEquals("ctsc-team-leader", RoleName.CTSC_TEAM_LEADER.getValue());
        assertEquals("national-business-centre", RoleName.NATIONAL_BUSINESS_CENTRE.getValue());
        assertEquals("challenged-access-ctsc", RoleName.CHALLENGED_ACCESS_CTSC.getValue());
        assertEquals("challenged-access-admin", RoleName.CHALLENGED_ACCESS_ADMIN.getValue());
        assertEquals("judge", RoleName.JUDGE.getValue());
        assertEquals("senior-judge", RoleName.SENIOR_JUDGE.getValue());
        assertEquals("leadership-judge", RoleName.LEADERSHIP_JUDGE.getValue());
        assertEquals("fee-paid-judge", RoleName.FEE_PAID_JUDGE.getValue());
        assertEquals("lead-judge", RoleName.LEAD_JUDGE.getValue());
        assertEquals("hearing-judge", RoleName.HEARING_JUDGE.getValue());
        assertEquals("ftpa-judge", RoleName.FTPA_JUDGE.getValue());
        assertEquals("hearing-panel-judge", RoleName.HEARING_PANEL_JUDGE.getValue());
        assertEquals("challenged-access-judiciary", RoleName.CHALLENGED_ACCESS_JUDICIARY.getValue());
        assertEquals("[LEGALREPRESENTATIVE]", RoleName.LEGAL_REPRESENTATIVE.getValue());
        assertEquals("[CREATOR]", RoleName.CREATOR.getValue());
        assertEquals(21, RoleName.values().length);
    }
}
