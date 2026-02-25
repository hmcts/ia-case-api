package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Builder
public class RoleRequest {
    private final String assignerId;
    private final String process;
    private final String reference;
    private final boolean replaceExisting;
}
