package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AssignmentTest {

    @Test
    void has_correct_values() {

        LocalDateTime now = LocalDateTime.now();

        Assignment assignment = new Assignment(
            "id",
            now,
            Collections.emptyList(),
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            RoleName.TRIBUNAL_CASEWORKER,
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            Collections.emptyMap()
        );

        assertEquals(
            "Assignment(id=id, "
            + "created=" + now.toString() + ","
            + " authorisations=[],"
            + " actorIdType=IDAM,"
            + " actorId=actorId,"
            + " roleType=CASE,"
            + " roleName=tribunal-caseworker,"
            + " roleCategory=JUDICIAL,"
            + " classification=PRIVATE,"
            + " grantType=BASIC,"
            + " readOnly=true,"
            + " attributes={}"
            + ")",
            assignment.toString()
        );

        assertEquals("id", assignment.getId());
        assertEquals(now, assignment.getCreated());
        assertEquals(Collections.emptyList(), assignment.getAuthorisations());
        assertEquals(ActorIdType.IDAM, assignment.getActorIdType());
        assertEquals("actorId", assignment.getActorId());
        assertEquals(RoleType.CASE, assignment.getRoleType());
        assertEquals(RoleName.TRIBUNAL_CASEWORKER, assignment.getRoleName());
        assertEquals(RoleCategory.JUDICIAL, assignment.getRoleCategory());
        assertEquals(Classification.PRIVATE, assignment.getClassification());
        assertEquals(GrantType.BASIC, assignment.getGrantType());
        assertEquals(Collections.<String, String>emptyMap(), assignment.getAttributes());
    }
}
