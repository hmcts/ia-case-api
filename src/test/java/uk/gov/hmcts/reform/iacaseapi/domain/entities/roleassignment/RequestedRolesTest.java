package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class RequestedRolesTest {

    @Test
    void has_correct_values() {

        RequestedRoles requestedRoles = new RequestedRoles(
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            "roleName",
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            Collections.emptyMap()
        );

        assertEquals(
            "RequestedRoles(actorIdType=IDAM, "
            + "actorId=actorId, "
            + "roleType=CASE, "
            + "roleName=roleName, "
            + "roleCategory=JUDICIAL, "
            + "classification=PRIVATE, "
            + "grantType=BASIC, "
            + "readOnly=true, "
            + "attributes={})",
            requestedRoles.toString()
        );
        assertEquals(ActorIdType.IDAM, requestedRoles.getActorIdType());
        assertEquals("actorId", requestedRoles.getActorId());
        assertEquals(RoleType.CASE, requestedRoles.getRoleType());
        assertEquals("roleName", requestedRoles.getRoleName());
        assertEquals(RoleCategory.JUDICIAL, requestedRoles.getRoleCategory());
        assertEquals(Classification.PRIVATE, requestedRoles.getClassification());
        assertEquals(GrantType.BASIC, requestedRoles.getGrantType());
        assertEquals(Collections.emptyMap(), requestedRoles.getAttributes());
    }
}
