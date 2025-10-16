package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class FasterCaseStatus {

    private YesOrNo status;
    private String reason;
    private String user;
    private String dateAdded;

    private FasterCaseStatus() {
    }

    public FasterCaseStatus(
        YesOrNo status,
        String reason,
        String user,
        String dateAdded
    ) {
        this.status = requireNonNull(status);
        this.reason = requireNonNull(reason);
        this.user = requireNonNull(user);
        this.dateAdded = requireNonNull(dateAdded);
    }

    public YesOrNo getFasterCaseStatus() {
        return requireNonNull(status);
    }

    public String getFasterCaseStatusReason() {
        return requireNonNull(reason);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateAdded() {
        return requireNonNull(dateAdded);
    }
}
