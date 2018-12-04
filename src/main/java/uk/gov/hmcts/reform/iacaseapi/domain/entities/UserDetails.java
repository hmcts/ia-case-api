package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class UserDetails {

    private final String forename;
    private final String surname;
    private final String emailAddress;

    public UserDetails(
        String forename,
        String surname,
        String emailAddress
    ) {
        requireNonNull(forename);
        requireNonNull(surname);
        requireNonNull(emailAddress);

        this.forename = forename;
        this.surname = surname;
        this.emailAddress = emailAddress;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
