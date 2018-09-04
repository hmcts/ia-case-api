package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class TimeExtensionReview {

    private Optional<String> grantOrDeny = Optional.empty();
    private Optional<String> dueDate = Optional.empty();
    private Optional<String> comment = Optional.empty();

    public TimeExtensionReview() {
        // noop
    }

    public Optional<String> getGrantOrDeny() {
        return grantOrDeny;
    }

    public Optional<String> getDueDate() {
        return dueDate;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public void setGrantOrDeny(String grantOrDeny) {
        this.grantOrDeny = Optional.ofNullable(grantOrDeny);
    }

    public void setDueDate(String dueDate) {
        this.dueDate = Optional.ofNullable(dueDate);
    }

    public void setComment(String comment) {
        this.comment = Optional.ofNullable(comment);
    }
}
