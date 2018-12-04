package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedEmailAddress = "john.doe@example.com";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("forename", expectedForename)
                .put("surname", expectedSurname)
                .put("email", expectedEmailAddress)
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

        assertEquals(expectedForename, actualUserDetails.getForename());
        assertEquals(expectedSurname, actualUserDetails.getSurname());
        assertEquals(expectedEmailAddress, actualUserDetails.getEmailAddress());
    }

    @Test
    public void should_throw_exception_if_idam_forename_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("surname", "Doe")
                .put("email", "john.doe@example.com")
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
            .hasMessage("IDAM user details missing 'forename' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_exception_if_idam_surname_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("forename", "John")
                .put("email", "john.doe@example.com")
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
            .hasMessage("IDAM user details missing 'surname' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_exception_if_idam_email_missing() {

        String accessToken = "ABCDEFG";

        Map<String, Object> userDetails =
            ImmutableMap
                .<String, Object>builder()
                .put("forename", "John")
                .put("surname", "Doe")
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
            .hasMessage("IDAM user details missing 'email' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
