package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;

public class TimeExtensionReview {

    private Optional<String> grantOrDeny = Optional.empty();
    private Optional<String> comment = Optional.empty();

    private TimeExtensionReview() {
        // noop
    }

    public Optional<String> getGrantOrDeny() {
        return grantOrDeny;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public void setGrantOrDeny(String grantOrDeny) {
        this.grantOrDeny = Optional.ofNullable(grantOrDeny);
    }

    public void setComment(String comment) {
        this.comment = Optional.ofNullable(comment);
    }
}
