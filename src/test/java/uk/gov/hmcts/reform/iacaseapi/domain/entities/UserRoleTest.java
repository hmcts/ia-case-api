package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UserRoleTest {

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    void to_string_gets_ids(UserRole userRole) {
        assertEquals(userRole.toString(), userRole.getId());
    }

    @Test
    void has_correct_values() {
        assertEquals("caseworker-ia-caseofficer", UserRole.CASE_OFFICER.toString());
        assertEquals("tribunal-caseworker", UserRole.TRIBUNAL_CASEWORKER.toString());
        assertEquals("challenged-access-legal-operations", UserRole.CHALLENGED_ACCESS_LEGAL_OPERATIONS.toString());
        assertEquals("senior-tribunal-caseworker", UserRole.SENIOR_TRIBUNAL_CASEWORKER.toString());
        assertEquals("caseworker-ia-admofficer", UserRole.ADMIN_OFFICER.toString());
        assertEquals("hearing-centre-admin", UserRole.HEARING_CENTRE_ADMIN.toString());
        assertEquals("ctsc", UserRole.CTSC.toString());
        assertEquals("ctsc-team-leader", UserRole.CTSC_TEAM_LEADER.toString());
        assertEquals("national-business-centre", UserRole.NATIONAL_BUSINESS_CENTRE.toString());
        assertEquals("challenged-access-ctsc", UserRole.CHALLENGED_ACCESS_CTSC.toString());
        assertEquals("challenged-access-admin", UserRole.CHALLENGED_ACCESS_ADMIN.toString());
        assertEquals("caseworker-ia-iacjudge", UserRole.IDAM_JUDGE.toString());
        assertEquals("caseworker-ia-judiciary", UserRole.JUDICIARY.toString());
        assertEquals("judge", UserRole.JUDGE.toString());
        assertEquals("senior-judge", UserRole.SENIOR_JUDGE.toString());
        assertEquals("leadership-judge", UserRole.LEADERSHIP_JUDGE.toString());
        assertEquals("fee-paid-judge", UserRole.FEE_PAID_JUDGE.toString());
        assertEquals("lead-judge", UserRole.LEAD_JUDGE.toString());
        assertEquals("hearing-judge", UserRole.HEARING_JUDGE.toString());
        assertEquals("ftpa-judge", UserRole.FTPA_JUDGE.toString());
        assertEquals("hearing-panel-judge", UserRole.HEARING_PANEL_JUDGE.toString());
        assertEquals("challenged-access-judiciary", UserRole.CHALLENGED_ACCESS_JUDICIARY.toString());
        assertEquals("caseworker-ia-legalrep-solicitor", UserRole.LEGAL_REPRESENTATIVE.toString());
        assertEquals("caseworker-ia-system", UserRole.SYSTEM.toString());
        assertEquals("caseworker-ia-homeofficeapc", UserRole.HOME_OFFICE_APC.toString());
        assertEquals("caseworker-ia-homeofficelart", UserRole.HOME_OFFICE_LART.toString());
        assertEquals("caseworker-ia-homeofficepou", UserRole.HOME_OFFICE_POU.toString());
        assertEquals("caseworker-ia-respondentofficer", UserRole.HOME_OFFICE_GENERIC.toString());
        assertEquals("citizen", UserRole.CITIZEN.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(29, UserRole.values().length);
    }
}
