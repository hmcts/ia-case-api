package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class UserDetailsTest {

    private final String forename = "forename";
    private final String surname = "surname";
    private final String emailAddress = "email@example.com";

    private UserDetails userDetails =
        new UserDetails(
            forename,
            surname,
            emailAddress
        );

    @Test
    public void should_hold_onto_values() {

        assertEquals(forename, userDetails.getForename());
        assertEquals(surname, userDetails.getSurname());
        assertEquals(emailAddress, userDetails.getEmailAddress());
    }
}
