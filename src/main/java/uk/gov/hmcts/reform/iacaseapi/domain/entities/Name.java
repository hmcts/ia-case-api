package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class Name {

    private Optional<String> title = Optional.empty();
    private Optional<String> firstName = Optional.empty();
    private Optional<String> lastName = Optional.empty();

    private Name() {
        // noop -- for deserializer
    }

    public Name(
        String title,
        String firstName,
        String lastName
    ) {
        this.title = Optional.ofNullable(title);
        this.firstName = Optional.ofNullable(firstName);
        this.lastName = Optional.ofNullable(lastName);
    }

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<String> getFirstName() {
        return firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }

    public void setTitle(String title) {
        this.title = Optional.ofNullable(title);
    }

    public void setFirstName(String firstName) {
        this.firstName = Optional.ofNullable(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = Optional.ofNullable(lastName);
    }
}
