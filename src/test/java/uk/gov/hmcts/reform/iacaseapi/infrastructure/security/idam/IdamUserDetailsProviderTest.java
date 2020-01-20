package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class IdamUserDetailsProviderTest {

    private static final String BASE_URL = "http://base.url";
    private static final String DETAILS_URI = "/details";

    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private RestTemplate restTemplate;

    private IdamUserDetailsProvider idamUserDetailsProvider;

    @Before
    public void setUp() {

        idamUserDetailsProvider =
            new IdamUserDetailsProvider(
                accessTokenProvider,
                restTemplate,
                BASE_URL,
                DETAILS_URI
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

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("uid", expectedId)
                .put("roles", expectedRoles)
                .put("sub", expectedEmailAddress)
                .put("given_name", expectedForename)
                .put("family_name", expectedSurname)
                .build();

        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        doReturn(new ResponseEntity<>(userDetails, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        UserDetails actualUserDetails = idamUserDetailsProvider.getUserDetails();

        ArgumentCaptor<HttpEntity> authorizeHttpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(BASE_URL + DETAILS_URI),
            eq(HttpMethod.GET),
            authorizeHttpEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity authorizeHttpEntity = authorizeHttpEntityCaptor.getAllValues().get(0);

        String actualAuthorizationHeader = authorizeHttpEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertEquals(expectedAccessToken, actualAuthorizationHeader);

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

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("roles", Arrays.asList("role"))
                .put("sub", "john.doe@example.com")
                .put("given_name", "John")
                .put("family_name", "Doe")
                .build();

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        doReturn(new ResponseEntity<>(userDetails, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'uid' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_exception_if_idam_roles_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("uid", "1234")
                .put("sub", "john.doe@example.com")
                .put("given_name", "John")
                .put("family_name", "Doe")
                .build();

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        doReturn(new ResponseEntity<>(userDetails, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'roles' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_exception_if_idam_email_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("uid", "1234")
                .put("roles", Arrays.asList("role"))
                .put("given_name", "John")
                .put("family_name", "Doe")
                .build();

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        doReturn(new ResponseEntity<>(userDetails, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'sub' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_exception_if_idam_forename_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("uid", "1234")
                .put("roles", Arrays.asList("role"))
                .put("sub", "john.doe@example.com")
                .put("family_name", "Doe")
                .build();

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        doReturn(new ResponseEntity<>(userDetails, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'given_name' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_exception_if_idam_surname_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("uid", "1234")
                .put("roles", Arrays.asList("role"))
                .put("sub", "john.doe@example.com")
                .put("given_name", "John")
                .build();

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        doReturn(new ResponseEntity<>(userDetails, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'family_name' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_wrap_server_exception_when_calling_idam() {

        HttpServerErrorException restClientException = mock(HttpServerErrorException.class);

        when(restTemplate
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(restClientException);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details with IDAM")
            .hasCause(restClientException);

    }

    @Test
    public void should_wrap_client_exception_when_calling_idam() {

        HttpClientErrorException restClientException = mock(HttpClientErrorException.class);

        when(restTemplate
            .exchange(
                eq(BASE_URL + DETAILS_URI),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(restClientException);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details with IDAM")
            .hasCause(restClientException);
    }
}
