package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RoleRequest {
    private final String assignerId;
    private final String process;
    private final String reference;
    private final boolean replaceExisting;

    public RoleRequest(String assignerId, String process, String reference, boolean replaceExisting) {
        this.assignerId = assignerId;
        this.process = process;
        this.reference = reference;
        this.replaceExisting = replaceExisting;
    }

    public String getAssignerId() {
        return assignerId;
    }

    public String getProcess() {
        return process;
    }

    public String getReference() {
        return reference;
    }

    public boolean isReplaceExisting() {
        return replaceExisting;
    }
}
