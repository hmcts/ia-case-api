package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
public class RoleAssignment {
    private final RoleRequest roleRequest;
    private final List<RequestedRoles> requestedRoles;
}
