package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;

class IdamUserDetailsTest {

    private final String accessToken = "access-token";
    private final String id = "1234";
    private final List<String> roles = Arrays.asList("role-1", "role-2");
    private final String emailAddress = "email@example.com";
    private final String forename = "forename";
    private final String surname = "surname";

    private IdamUserDetails userDetails =
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

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {
        "ADMIN_OFFICER",
        "HEARING_CENTRE_ADMIN",
        "CTSC",
        "CTSC_TEAM_LEADER",
        "NATIONAL_BUSINESS_CENTRE",
        "CHALLENGED_ACCESS_CTSC",
        "CHALLENGED_ACCESS_ADMIN"
    })
    void isAdmin_should_be_true_for_admin_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertTrue(userDetails.isAdmin());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "ADMIN_OFFICER",
        "HEARING_CENTRE_ADMIN",
        "CTSC",
        "CTSC_TEAM_LEADER",
        "NATIONAL_BUSINESS_CENTRE",
        "CHALLENGED_ACCESS_CTSC",
        "CHALLENGED_ACCESS_ADMIN"
    })
    void isAdmin_should_be_false_for_non_admin_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertFalse(userDetails.isAdmin());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {
        "HOME_OFFICE_APC",
        "HOME_OFFICE_LART",
        "HOME_OFFICE_POU",
        "HOME_OFFICE_GENERIC"
    })
    void isHomeOffice_should_be_true_for_ho_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertTrue(userDetails.isHomeOffice());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "HOME_OFFICE_APC",
        "HOME_OFFICE_LART",
        "HOME_OFFICE_POU",
        "HOME_OFFICE_GENERIC"
    })
    void isHomeOffice_should_be_false_for_ho_roles(UserRole role) {
        userDetails =
            new IdamUserDetails(
                accessToken,
                id,
                Collections.singletonList(role.getId()),
                emailAddress,
                forename,
                surname
            );
        assertFalse(userDetails.isHomeOffice());
    }
}
