package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class FasterCaseStatus {

    private Boolean fasterCaseStatus;
    private String fasterCaseStatusReason;
    private String user;
    private String dateAdded;

    private FasterCaseStatus() {
    }

    public FasterCaseStatus(
        Boolean fasterCaseStatus,
        String fasterCaseStatusReason,
        String user,
        String dateAdded
    ) {
        this.fasterCaseStatus = requireNonNull(fasterCaseStatus);
        this.fasterCaseStatusReason = requireNonNull(fasterCaseStatusReason);
        this.user = requireNonNull(user);
        this.dateAdded = requireNonNull(dateAdded);
    }

    public Boolean getFasterCaseStatus() {
        return requireNonNull(fasterCaseStatus);
    }

    public String getFasterCaseStatusReason() {
        return requireNonNull(fasterCaseStatusReason);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateAdded() {
        return requireNonNull(dateAdded);
    }
}
