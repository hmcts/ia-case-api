package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleNameTest {

    @Test
    void has_correct_values() {
        assertEquals(RoleName.TRIBUNAL_CASEWORKER.toString(), RoleName.TRIBUNAL_CASEWORKER.getValue());
        assertEquals(RoleName.LEGAL_REPRESENTATIVE.toString(), RoleName.LEGAL_REPRESENTATIVE.getValue());
        assertEquals(RoleName.SENIOR_TRIBUNAL_CASEWORKER.toString(), RoleName.SENIOR_TRIBUNAL_CASEWORKER.getValue());

        assertEquals("tribunal-caseworker", RoleName.TRIBUNAL_CASEWORKER.toString());
        assertEquals("senior-tribunal-caseworker", RoleName.SENIOR_TRIBUNAL_CASEWORKER.toString());

    }
}
