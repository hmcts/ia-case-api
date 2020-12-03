package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IdamUserDetailsProviderTest {

    @Mock
    private AccessTokenProvider accessTokenProvider;
    @Mock
    private IdamApi idamApi;

    private IdamUserDetailsProvider idamUserDetailsProvider;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        idamUserDetailsProvider =
            new IdamUserDetailsProvider(
                accessTokenProvider,
                idamApi
            );
    }

    @Test
    void should_call_idam_api_to_get_user_details() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo userInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(idamApi.userInfo(expectedAccessToken)).thenReturn(userInfo);

        UserDetails actualUserDetails = idamUserDetailsProvider.getUserDetails();

        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedAccessToken, actualUserDetails.getAccessToken());
        assertEquals(expectedId, actualUserDetails.getId());
        assertEquals(expectedRoles, actualUserDetails.getRoles());
        assertEquals(expectedEmailAddress, actualUserDetails.getEmailAddress());
        assertEquals(expectedForename, actualUserDetails.getForename());
        assertEquals(expectedSurname, actualUserDetails.getSurname());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-caseofficer",
        "caseworker-ia-admofficer",
        "caseworker-ia-iacjudge",
        "caseworker-ia-judiciary",
        "caseworker-ia-legalrep-solicitor",
        "caseworker-ia-system"
    })
    void should_get_logged_in_with_a_valid_user_role(String roleName) {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList(roleName, "role-2");

        UserInfo userInfo = new UserInfo("", expectedId,
            expectedRoles, "", "", "");

        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);
        when(idamApi.userInfo(expectedAccessToken)).thenReturn(userInfo);

        assertEquals(roleName, idamUserDetailsProvider.getLoggedInUserRole().toString());

        switch (roleName) {
            case "caseworker-ia-caseofficer":
                assertEquals("Tribunal Caseworker", idamUserDetailsProvider.getLoggedInUserRoleLabel().toString());
                break;

            case "caseworker-ia-admofficer":
                assertEquals("Admin Officer", idamUserDetailsProvider.getLoggedInUserRoleLabel().toString());
                break;

            case "caseworker-ia-iacjudge":
            case "caseworker-ia-judiciary":
                assertEquals("Judge", idamUserDetailsProvider.getLoggedInUserRoleLabel().toString());
                break;

            case "caseworker-ia-legalrep-solicitor":
                assertEquals("Legal representative", idamUserDetailsProvider.getLoggedInUserRoleLabel().toString());
                break;

            default:
                assertEquals("System", idamUserDetailsProvider.getLoggedInUserRoleLabel().toString());
        }
    }

    @Test
    void should_get_logged_in_user_role_unknown() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList("caseworker-ia-unknown", "role-2");

        UserInfo userInfo = new UserInfo("", expectedId,
            expectedRoles, "", "", "");

        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);
        when(idamApi.userInfo(expectedAccessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getLoggedInUserRole())
            .hasMessage("No valid user role is present.")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "caseworker-ia-respondentofficer", "caseworker-ia-homeofficeapc", "caseworker-ia-homeofficelart",
        "caseworker-ia-homeofficepou"
    })
    void should_get_logged_in_user_role_home_office_generic(String emailId) {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList(emailId, "role-2");

        UserInfo userInfo = new UserInfo("", expectedId,
            expectedRoles, "", "", "");

        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);
        when(idamApi.userInfo(expectedAccessToken)).thenReturn(userInfo);

        assertEquals(emailId, idamUserDetailsProvider.getLoggedInUserRole().toString());
        assertEquals("Respondent", idamUserDetailsProvider.getLoggedInUserRoleLabel().toString());
    }

    @Test
    void should_throw_exception_if_idam_id_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            "john.doe@example.com",
            null,
            Arrays.asList("role"),
            "John Doe",
            "John",
            "Doe"
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'uid' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_if_idam_roles_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            "john.doe@example.com",
            "some-id",
            null,
            "John Doe",
            "John",
            "Doe"
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'roles' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_if_idam_email_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            null,
            "some-id",
            Arrays.asList("role"),
            "John Doe",
            "John",
            "Doe"
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'sub' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_if_idam_forename_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            "john.doe@example.com",
            "some-id",
            Arrays.asList("role"),
            "John Doe",
            null,
            "Doe"
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'given_name' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_if_idam_surname_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            "john.doe@example.com",
            "some-id",
            Arrays.asList("role"),
            "John Doe",
            "John",
            null
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);


        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'family_name' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_wrap_server_exception_when_calling_idam() {

        String accessToken = "ABCDEFG";

        FeignException restClientException = mock(FeignException.class);

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(anyString())).thenThrow(restClientException);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details with IDAM")
            .hasCause(restClientException);
    }

    @Test
    void should_wrap_client_exception_when_calling_idam() {

        String accessToken = "ABCDEFG";

        FeignException restClientException = mock(FeignException.class);

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(anyString())).thenThrow(restClientException);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details with IDAM")
            .hasCause(restClientException);
    }
}
