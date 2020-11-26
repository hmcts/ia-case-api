package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RoleRequest {
    private String assignerId;
    private String process;
    private String reference;
    private boolean replaceExisting;

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
