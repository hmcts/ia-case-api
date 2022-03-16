package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdamUserDetailsTest {

    private final String accessToken = "access-token";
    private final String id = "1234";
    private final List<String> roles = Arrays.asList("role-1", "role-2");

    private IdamUserDetails userDetails =
        new IdamUserDetails(
            accessToken,
            id,
            roles
        );

    @Test
    void should_hold_onto_values() {

        assertEquals(accessToken, userDetails.getAccessToken());
        assertEquals(id, userDetails.getId());
        assertEquals(roles, userDetails.getRoles());
    }
}
