package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class IdamAuthorizorTest {

    private static final String BASE_URL = "http://base.url";
    private static final String CLIENT_ID = "1234";
    private static final String CLIENT_SECRET = "badgers";
    private static final String CLIENT_REDIRECT_URI = "http://redirect.url";

    @Mock private RestTemplate restTemplate;

    private IdamAuthorizor idamAuthorizor;

    @Before
    public void setUp() {

        idamAuthorizor =
            new IdamAuthorizor(
                restTemplate,
                BASE_URL,
                CLIENT_ID,
                CLIENT_SECRET,
                CLIENT_REDIRECT_URI
            );
    }

    @Test
    public void should_call_idam_api_to_authorize() {

        String username = "username";
        String password = "password";

        doReturn(new ResponseEntity<>(ImmutableMap.of("access_token", "XYZ"), HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + "/o/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        String actualAccessToken = idamAuthorizor.exchangeForAccessToken(username, password);

        assertEquals("Bearer XYZ", actualAccessToken);

        ArgumentCaptor<HttpEntity> tokenHttpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(BASE_URL + "/o/token"),
            eq(HttpMethod.POST),
            tokenHttpEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity tokenHttpEntity = tokenHttpEntityCaptor.getAllValues().get(0);

        HttpHeaders actualTokenHeaders = tokenHttpEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, actualTokenHeaders.getContentType());

        MultiValueMap actualTokenParameters = (MultiValueMap) tokenHttpEntity.getBody();
        assertEquals(username, actualTokenParameters.getFirst("username"));
        assertEquals(password, actualTokenParameters.getFirst("password"));
        assertEquals("openid profile roles", actualTokenParameters.getFirst("scope"));
        assertEquals("password", actualTokenParameters.getFirst("grant_type"));
        assertEquals(CLIENT_REDIRECT_URI, actualTokenParameters.getFirst("redirect_uri"));
        assertEquals(CLIENT_ID, actualTokenParameters.getFirst("client_id"));
        assertEquals(CLIENT_SECRET, actualTokenParameters.getFirst("client_secret"));
    }

    @Test
    public void wrap_client_exception_when_calling_oauth_token() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        String username = "username";
        String password = "password";

        when(restTemplate
            .exchange(
                eq(BASE_URL + "/o/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> idamAuthorizor.exchangeForAccessToken(username, password))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth token with IDAM");

    }

    @Test
    public void wrap_server_exception_when_calling_oauth_token() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        String username = "username";
        String password = "password";

        when(restTemplate
            .exchange(
                eq(BASE_URL + "/o/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> idamAuthorizor.exchangeForAccessToken(username, password))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth token with IDAM");

    }

}
