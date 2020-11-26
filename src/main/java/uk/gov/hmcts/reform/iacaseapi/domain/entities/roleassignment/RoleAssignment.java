package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RoleAssignment {
    private RoleRequest roleRequest;
    private List<RequestedRoles> requestedRoles;

    public RoleAssignment(RoleRequest roleRequest, List<RequestedRoles> requestedRoles) {
        this.roleRequest = roleRequest;
        this.requestedRoles = requestedRoles;
    }

    public RoleRequest getRoleRequest() {
        return roleRequest;
    }

    public List<RequestedRoles> getRequestedRoles() {
        return requestedRoles;
    }
}
