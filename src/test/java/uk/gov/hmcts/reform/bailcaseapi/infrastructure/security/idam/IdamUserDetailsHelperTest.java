package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;

@ExtendWith(MockitoExtension.class)
class IdamUserDetailsHelperTest {

    @Mock private UserDetails userDetails;

    private IdamUserDetailsHelper idamUserDetailsHelper = new IdamUserDetailsHelper();

    @Test
    public void should_get_logged_in_with_a_valid_user_role() {

        Stream.of(
            "caseworker-ia-caseofficer",
            "caseworker-ia-admofficer",
            "caseworker-ia-iacjudge",
            "caseworker-ia-judiciary",
            "caseworker-ia-legalrep-solicitor",
            "caseworker-ia-system"
        ).forEach(roleName -> {
            List<String> expectedRoles = Arrays.asList(roleName, "role-2");

            when(userDetails.getRoles()).thenReturn(expectedRoles);

            assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());

            switch (roleName) {
                case "caseworker-ia-caseofficer":
                    assertEquals("Tribunal Caseworker",
                        idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
                    break;

                case "caseworker-ia-admofficer":
                    assertEquals("Admin Officer",
                        idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
                    break;

                case "caseworker-ia-iacjudge":
                case "caseworker-ia-judiciary":
                    assertEquals("Judge",
                        idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
                    break;

                case "caseworker-ia-legalrep-solicitor":
                    assertEquals("Legal Representative",
                        idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
                    break;

                default:
                    assertEquals("System",
                        idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
            }
        });
    }

    @Test
    public void should_get_logged_in_user_role_unknown() {

        List<String> expectedRoles = Arrays.asList("caseworker-ia-unknown", "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertThatThrownBy(() -> idamUserDetailsHelper.getLoggedInUserRole(userDetails))
            .hasMessage("No valid user role is present.")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_get_logged_in_user_role_home_office_bail() {

        Stream.of(
            "caseworker-ia-homeofficebail"
        ).forEach(roleName -> {
            List<String> expectedRoles = Arrays.asList(roleName, "role-2");

            when(userDetails.getRoles()).thenReturn(expectedRoles);

            assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());
            assertEquals("Home Office", idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
        });

    }

    @Test
    public void should_get_idam_username() {

        when(userDetails.getForename()).thenReturn("First name");
        when(userDetails.getSurname()).thenReturn("Surname");

        assertEquals("First name" + " " + "Surname", idamUserDetailsHelper.getIdamUserName(userDetails));
    }
}
