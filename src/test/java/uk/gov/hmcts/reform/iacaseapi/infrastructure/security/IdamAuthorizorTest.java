package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.Base64;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("unchecked")
public class IdamAuthorizorTest {

    private static final String BASE_URL = "http://base.url";
    private static final String CLIENT_ID = "1234";
    private static final String CLIENT_SECRET = "badgers";
    private static final String CLIENT_REDIRECT_URI = "http://redirect.url";

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final IdamAuthorizor idamAuthorizor =
        new IdamAuthorizor(
            restTemplate,
            BASE_URL,
            CLIENT_ID,
            CLIENT_SECRET,
            CLIENT_REDIRECT_URI
        );

    @Test
    public void should_call_idam_api_to_authorize() {

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

        assertEquals(actualAccessToken, "Bearer XYZ");

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
}
