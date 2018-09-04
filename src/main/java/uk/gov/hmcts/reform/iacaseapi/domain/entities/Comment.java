package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class Comment {

    private Optional<String> author = Optional.empty();
    private Optional<String> role = Optional.empty();
    private Optional<String> comment = Optional.empty();

    private Comment() {
        // noop -- for deserializer
    }

    public Optional<String> getAuthor() {
        return author;
    }

    public Optional<String> getRole() {
        return role;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public void setAuthor(String author) {
        this.author = Optional.ofNullable(author);
    }

    public void setRole(String role) {
        this.role = Optional.ofNullable(role);
    }

    public void setComment(String comment) {
        this.comment = Optional.ofNullable(comment);
    }
}
