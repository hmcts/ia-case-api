package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static junit.framework.TestCase.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdamUserDetailsTest {

    final String accessToken = "access-token";
    final String id = "1234";
    final List<String> roles = Arrays.asList("role-1", "role-2");
    final String emailAddress = "email@example.com";
    final String forename = "forename";
    final String surname = "surname";

    IdamUserDetails userDetails =
        new IdamUserDetails(
            accessToken,
            id,
            roles,
            emailAddress,
            forename,
            surname
        );

    @Test
    void should_hold_onto_values() {

        assertEquals(accessToken, userDetails.getAccessToken());
        assertEquals(id, userDetails.getId());
        assertEquals(roles, userDetails.getRoles());
        assertEquals(emailAddress, userDetails.getEmailAddress());
        assertEquals(forename, userDetails.getForename());
        assertEquals(surname, userDetails.getSurname());
    }
}
