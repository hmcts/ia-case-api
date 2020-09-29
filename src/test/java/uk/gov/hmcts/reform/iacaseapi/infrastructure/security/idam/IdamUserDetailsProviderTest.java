package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import feign.FeignException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@RunWith(MockitoJUnitRunner.class)
public class IdamUserDetailsProviderTest {

    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private IdamApi idamApi;

    private IdamUserDetailsProvider idamUserDetailsProvider;

    @Before
    public void setUp() {

        idamUserDetailsProvider =
            new IdamUserDetailsProvider(
                accessTokenProvider,
                idamApi
            );
    }

    @Test
    public void should_call_idam_api_to_get_user_details() {

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

    @Test
    public void should_throw_exception_if_idam_id_missing() {

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
    public void should_throw_exception_if_idam_roles_missing() {

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
    public void should_throw_exception_if_idam_email_missing() {

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
    public void should_throw_exception_if_idam_forename_missing() {

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
    public void should_throw_exception_if_idam_surname_missing() {

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
    public void should_wrap_server_exception_when_calling_idam() {

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
    public void should_wrap_client_exception_when_calling_idam() {

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
