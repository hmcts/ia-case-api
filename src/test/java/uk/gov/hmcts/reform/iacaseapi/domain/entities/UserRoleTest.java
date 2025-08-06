package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class UserRoleTest {

    @ParameterizedTest
    @EnumSource(value = UserRole.class)
    void to_string_gets_ids(UserRole userRole) {
        assertEquals(userRole.toString(), userRole.getId());
    }

    @ParameterizedTest
    @CsvSource({
        "caseworker-ia-caseofficer, CASE_OFFICER",
        "tribunal-caseworker, TRIBUNAL_CASEWORKER",
        "challenged-access-legal-operations, CHALLENGED_ACCESS_LEGAL_OPERATIONS",
        "senior-tribunal-caseworker, SENIOR_TRIBUNAL_CASEWORKER",
        "caseworker-ia-admofficer, ADMIN_OFFICER",
        "hearing-centre-admin, HEARING_CENTRE_ADMIN",
        "ctsc, CTSC",
        "ctsc-team-leader, CTSC_TEAM_LEADER",
        "national-business-centre, NATIONAL_BUSINESS_CENTRE",
        "challenged-access-ctsc, CHALLENGED_ACCESS_CTSC",
        "challenged-access-admin, CHALLENGED_ACCESS_ADMIN",
        "caseworker-ia-iacjudge, IDAM_JUDGE",
        "caseworker-ia-judiciary, JUDICIARY",
        "judge, JUDGE",
        "senior-judge, SENIOR_JUDGE",
        "leadership-judge, LEADERSHIP_JUDGE",
        "fee-paid-judge, FEE_PAID_JUDGE",
        "lead-judge, LEAD_JUDGE",
        "hearing-judge, HEARING_JUDGE",
        "ftpa-judge, FTPA_JUDGE",
        "hearing-panel-judge, HEARING_PANEL_JUDGE",
        "challenged-access-judiciary, CHALLENGED_ACCESS_JUDICIARY",
        "caseworker-ia-legalrep-solicitor, LEGAL_REPRESENTATIVE",
        "caseworker-ia-system, SYSTEM",
        "caseworker-ia-homeofficeapc, HOME_OFFICE_APC",
        "caseworker-ia-homeofficelart, HOME_OFFICE_LART",
        "caseworker-ia-homeofficepou, HOME_OFFICE_POU",
        "caseworker-ia-respondentofficer, HOME_OFFICE_GENERIC",
        "citizen, CITIZEN",
        "unknown, UNKNOWN"
    })
    void has_correct_values(String expectedId, UserRole userRole) {
        assertEquals(expectedId, userRole.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(30, UserRole.values().length);
    }

    @Test
    void get_admin_roles_correct_values() {
        assertTrue(UserRole.getAdminRoles().contains("caseworker-ia-admofficer"));
        assertTrue(UserRole.getAdminRoles().contains("hearing-centre-admin"));
        assertTrue(UserRole.getAdminRoles().contains("ctsc"));
        assertTrue(UserRole.getAdminRoles().contains("ctsc-team-leader"));
        assertTrue(UserRole.getAdminRoles().contains("national-business-centre"));
        assertTrue(UserRole.getAdminRoles().contains("challenged-access-ctsc"));
        assertTrue(UserRole.getAdminRoles().contains("challenged-access-admin"));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes_admin_roles() {
        assertEquals(7, UserRole.getAdminRoles().size());
    }

    @Test
    void get_ho_roles_correct_values() {
        assertTrue(UserRole.getHomeOfficeRoles().contains("caseworker-ia-homeofficeapc"));
        assertTrue(UserRole.getHomeOfficeRoles().contains("caseworker-ia-homeofficelart"));
        assertTrue(UserRole.getHomeOfficeRoles().contains("caseworker-ia-homeofficepou"));
        assertTrue(UserRole.getHomeOfficeRoles().contains("caseworker-ia-respondentofficer"));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes_ho_roles() {
        assertEquals(4, UserRole.getHomeOfficeRoles().size());
    }
}
