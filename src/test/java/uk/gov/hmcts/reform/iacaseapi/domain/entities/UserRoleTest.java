package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class UserRoleTest {

    @Test
    public void has_correct_values() {
        assertEquals("caseworker-ia-caseofficer", UserRole.CASE_OFFICER.toString());
        assertEquals("caseworker-ia-legalrep-solicitor", UserRole.LEGAL_REPRESENTATIVE.toString());
        assertEquals("caseworker-ia-judiciary", UserRole.JUDICIARY.toString());
        assertEquals("caseworker-ia-system", UserRole.SYSTEM.toString());
        assertEquals("caseworker-ia-admofficer", UserRole.ADMIN_OFFICER.toString());
        assertEquals("caseworker-ia-homeofficeapc", UserRole.HOME_OFFICE_APC.toString());
        assertEquals("caseworker-ia-homeofficelart", UserRole.HOME_OFFICE_LART.toString());
        assertEquals("caseworker-ia-homeofficepou", UserRole.HOME_OFFICE_POU.toString());
        assertEquals("caseworker-ia-respondentofficer", UserRole.HOME_OFFICE_GENERIC.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(9, UserRole.values().length);
    }
}
