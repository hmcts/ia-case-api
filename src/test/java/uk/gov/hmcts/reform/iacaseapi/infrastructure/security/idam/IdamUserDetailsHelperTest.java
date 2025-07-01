package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;

@ExtendWith(MockitoExtension.class)
class IdamUserDetailsHelperTest {

    @Mock
    private UserDetails userDetails;

    private IdamUserDetailsHelper idamUserDetailsHelper = new IdamUserDetailsHelper();

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-legalrep-solicitor",
        "caseworker-ia-system",
        "citizen"
    })
    public void should_get_logged_in_with_a_valid_user_role(String roleName) {
        List<String> expectedRoles = Arrays.asList(roleName, "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());

        switch (roleName) {
            case "caseworker-ia-legalrep-solicitor":
                assertEquals("Legal representative", idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
                break;
            case "citizen":
                assertEquals("Appellant", idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
                break;
            default:
                assertEquals("System", idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString());
        }
    }

    @Test
    public void should_get_logged_in_user_role_unknown() {

        List<String> expectedRoles = Arrays.asList("caseworker-ia-unknown", "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertThatThrownBy(() -> idamUserDetailsHelper.getLoggedInUserRole(userDetails))
            .hasMessage("No valid user role is present.")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-respondentofficer",
        "caseworker-ia-homeofficeapc",
        "caseworker-ia-homeofficelart",
        "caseworker-ia-homeofficepou"
    })
    public void should_get_logged_in_user_role_home_office_generic(String roleName) {
        List<String> expectedRoles = Arrays.asList(roleName, "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());
        assertEquals(UserRoleLabel.HOME_OFFICE_GENERIC, idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-admofficer",
        "hearing-centre-admin",
        "ctsc",
        "ctsc-team-leader",
        "national-business-centre",
        "challenged-access-ctsc",
        "challenged-access-admin"
    })
    public void should_get_logged_in_user_role_admin(String roleName) {
        List<String> expectedRoles = Arrays.asList(roleName, "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());
        assertEquals(UserRoleLabel.ADMIN_OFFICER, idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-iacjudge",
        "caseworker-ia-judiciary",
        "judge",
        "senior-judge",
        "leadership-judge",
        "fee-paid-judge",
        "lead-judge",
        "hearing-judge",
        "ftpa-judge",
        "hearing-panel-judge",
        "challenged-access-judiciary"
    })
    public void should_get_logged_in_user_role_judge(String roleName) {
        List<String> expectedRoles = Arrays.asList(roleName, "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());
        assertEquals(UserRoleLabel.JUDGE, idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-caseofficer",
        "tribunal-caseworker",
        "challenged-access-legal-operations",
        "senior-tribunal-caseworker"
    })
    public void should_get_logged_in_user_role_case_officer(String roleName) {
        List<String> expectedRoles = Arrays.asList(roleName, "role-2");

        when(userDetails.getRoles()).thenReturn(expectedRoles);

        assertEquals(roleName, idamUserDetailsHelper.getLoggedInUserRole(userDetails).toString());
        assertEquals(UserRoleLabel.TRIBUNAL_CASEWORKER, idamUserDetailsHelper.getLoggedInUserRoleLabel(userDetails));
    }

}
