package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class UserRoleLabelTest {

    @Test
    public void has_correct_values() {
        assertEquals("Judge", UserRoleLabel.JUDGE.toString());
        assertEquals("Tribunal Caseworker", UserRoleLabel.TRIBUNAL_CASEWORKER.toString());
        assertEquals("Admin Officer", UserRoleLabel.ADMIN_OFFICER.toString());
        assertEquals("Respondent", UserRoleLabel.HOME_OFFICE_GENERIC.toString());
        assertEquals("Legal representative", UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
        assertEquals("System", UserRoleLabel.SYSTEM.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, UserRoleLabel.values().length);
    }
}
