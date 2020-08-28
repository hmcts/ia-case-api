package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Base64;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IdamAuthorizorTest {

    static final String BASE_URL = "http://base.url";
    static final String CLIENT_ID = "1234";
    static final String CLIENT_SECRET = "badgers";
    static final String CLIENT_REDIRECT_URI = "http://redirect.url";

    @Mock private RestTemplate restTemplate;

    IdamAuthorizor idamAuthorizor;

    @BeforeEach
    void setUp() {

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
    void should_call_idam_api_to_authorize() {

        String username = "username";
        String password = "password";

        doReturn(new ResponseEntity<>(ImmutableMap.of("code", "ABC"), HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + "/oauth2/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        doReturn(new ResponseEntity<>(ImmutableMap.of("access_token", "XYZ"), HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + "/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        String actualAccessToken = idamAuthorizor.exchangeForAccessToken(username, password);

        assertEquals("Bearer XYZ", actualAccessToken);

        ArgumentCaptor<HttpEntity> authorizeHttpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(BASE_URL + "/oauth2/authorize"),
            eq(HttpMethod.POST),
            authorizeHttpEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity authorizeHttpEntity = authorizeHttpEntityCaptor.getAllValues().get(0);

        HttpHeaders actualAuthorizeHeaders = authorizeHttpEntity.getHeaders();
        String actualAuthorizationHeader = authorizeHttpEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(actualAuthorizationHeader, CoreMatchers.startsWith("Basic "));
        assertEquals("username:password", new String(Base64.getDecoder().decode(actualAuthorizationHeader.replaceFirst("Basic ", ""))));

        MultiValueMap actualAuthorizeParameters = (MultiValueMap) authorizeHttpEntity.getBody();
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, actualAuthorizeHeaders.getContentType());
        assertEquals("code", actualAuthorizeParameters.getFirst("response_type"));
        assertEquals(CLIENT_ID, actualAuthorizeParameters.getFirst("client_id"));
        assertEquals(CLIENT_REDIRECT_URI, actualAuthorizeParameters.getFirst("redirect_uri"));

        ArgumentCaptor<HttpEntity> tokenHttpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(BASE_URL + "/oauth2/token"),
            eq(HttpMethod.POST),
            tokenHttpEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity tokenHttpEntity = tokenHttpEntityCaptor.getAllValues().get(0);

        HttpHeaders actualTokenHeaders = tokenHttpEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, actualTokenHeaders.getContentType());

        MultiValueMap actualTokenParameters = (MultiValueMap) tokenHttpEntity.getBody();
        assertEquals("ABC", actualTokenParameters.getFirst("code"));
        assertEquals("authorization_code", actualTokenParameters.getFirst("grant_type"));
        assertEquals(CLIENT_REDIRECT_URI, actualTokenParameters.getFirst("redirect_uri"));
        assertEquals(CLIENT_ID, actualTokenParameters.getFirst("client_id"));
        assertEquals(CLIENT_SECRET, actualTokenParameters.getFirst("client_secret"));
    }

    @Test
    void wrap_client_exception_when_calling_oauth_authorize() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        String username = "username";
        String password = "password";

        when(restTemplate
            .exchange(
                eq(BASE_URL + "/oauth2/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(underlyingException);

        assertThatThrownBy(() -> idamAuthorizor.exchangeForAccessToken(username, password))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth code with IDAM");

    }

    @Test
    void wrap_server_exception_when_calling_oauth_authorize() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        String username = "username";
        String password = "password";

        when(restTemplate
            .exchange(
                eq(BASE_URL + "/oauth2/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenThrow(underlyingException);

        assertThatThrownBy(() -> idamAuthorizor.exchangeForAccessToken(username, password))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth code with IDAM");

    }

    @Test
    void wrap_client_exception_when_calling_oauth_token() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        String username = "username";
        String password = "password";

        doReturn(new ResponseEntity<>(ImmutableMap.of("code", "ABC"), HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + "/oauth2/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        when(restTemplate
            .exchange(
                eq(BASE_URL + "/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> idamAuthorizor.exchangeForAccessToken(username, password))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth token with IDAM");

    }

    @Test
    void wrap_server_exception_when_calling_oauth_token() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        String username = "username";
        String password = "password";

        doReturn(new ResponseEntity<>(ImmutableMap.of("code", "ABC"), HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(BASE_URL + "/oauth2/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        when(restTemplate
            .exchange(
                eq(BASE_URL + "/oauth2/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> idamAuthorizor.exchangeForAccessToken(username, password))
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get auth token with IDAM");

    }


}
