package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;

public class CaseSummary {

    private Optional<List<String>> groundsForAppeal = Optional.empty();
    private Optional<List<String>> issues = Optional.empty();
    private Optional<List<Comment>> comments = Optional.empty();

    private CaseSummary() {
        // noop -- for deserializer
    }

    public Optional<List<String>> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public Optional<List<String>> getIssues() {
        return issues;
    }

    public Optional<List<Comment>> getComments() {
        return comments;
    }

    public void setGroundsForAppeal(List<String> groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }

    public void setIssues(List<String> issues) {
        this.issues = Optional.ofNullable(issues);
    }

    public void setComments(List<Comment> comments) {
        this.comments = Optional.ofNullable(comments);
    }
}
