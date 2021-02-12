package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleRequestTest {

    @Test
    void has_correct_values() {

        RoleRequest roleRequest = new RoleRequest(
            "assignId", "process", "ref", true
        );


        assertEquals("RoleRequest(assignerId=assignId, process=process, reference=ref, replaceExisting=true)", roleRequest.toString());
        assertEquals("assignId", roleRequest.getAssignerId());
        assertEquals("process", roleRequest.getProcess());
        assertEquals("ref", roleRequest.getReference());
        assertTrue(roleRequest.isReplaceExisting());
    }
}
