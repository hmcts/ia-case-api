package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class BailCaseCallbackApiDelegatorTest {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String ENDPOINT = "http://endpoint";

    private BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;

    @Mock private AccessTokenProvider accessTokenProvider;

    @Mock private RestTemplate restTemplate;

    @Mock private Callback<BailCase> callback;

    @Mock private PreSubmitCallbackResponse<BailCase> callbackResponse;

    @Mock private PostSubmitCallbackResponse postSubmitCallbackResponse;

    private final String secret = "s2s-secret";
    private final String microService = "micro-service";
    private final String authUrl = "http://127.0.0.1:4502";

    public BailCaseCallbackApiDelegatorTest() {
    }

    @BeforeEach
    public void setUp() {
        bailCaseCallbackApiDelegator = new BailCaseCallbackApiDelegator(serviceAuthTokenGenerator,
                                                                        accessTokenProvider,
                                                                        restTemplate,
                                                                        secret,
                                                                        microService,
                                                                        authUrl
        );
    }

    @Test
    void should_call_documents_api_to_generate_documents() {

        final String serviceToken = "ABCDEFG";
        final String accessToken = "HIJKLMN";
        final BailCase submittedCase = mock(BailCase.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(serviceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);
        when(callbackResponse.getData()).thenReturn(submittedCase);

        doReturn(
            new ResponseEntity<>(
                callbackResponse,
                HttpStatus.OK
            ))
            .when(restTemplate)
            .exchange(eq(ENDPOINT),
                      eq(HttpMethod.POST),
                      any(HttpEntity.class),
                      any(ParameterizedTypeReference.class)
            );

        final BailCase bailCase = bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT);

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(eq(ENDPOINT),
                                                eq(HttpMethod.POST),
                                                requestEntityCaptor.capture(),
                                                any(ParameterizedTypeReference.class)
        );

        HttpEntity actualEntity = requestEntityCaptor.getAllValues().get(0);
        final String actualContentTypeHeader = actualEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualAuthorizationHeader = actualEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final String actualServiceAuthHeader = actualEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final Callback<BailCase> actualPostBody = (Callback<BailCase>) actualEntity.getBody();

        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualContentTypeHeader);
        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualAcceptHeader);
        assertEquals(accessToken, actualAuthorizationHeader);
        assertEquals(serviceToken, actualServiceAuthHeader);
        assertEquals(callback, actualPostBody);
        assertEquals(submittedCase, bailCase);
    }

    @Test
    void should_call_notifications_api_to_send_notifications_in_ccd_submitted() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final PostSubmitCallbackResponse notifiedSubmitCallbackResponse = mock(PostSubmitCallbackResponse.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        doReturn(new ResponseEntity<>(postSubmitCallbackResponse, HttpStatus.OK)).when(restTemplate).exchange(eq(
            ENDPOINT), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));

        final PostSubmitCallbackResponse actualPostSubmitCallbackResponse = bailCaseCallbackApiDelegator
            .delegatePostSubmit(
                callback,
                ENDPOINT
            );

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(eq(ENDPOINT),
                                                eq(HttpMethod.POST),
                                                requestEntityCaptor.capture(),
                                                any(ParameterizedTypeReference.class)
        );

        HttpEntity actualRequestEntity = requestEntityCaptor.getAllValues().get(0);

        final String actualContentTypeHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualServiceAuthHeader = actualRequestEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final String actualAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        final Callback<BailCase> actualPostBody = (Callback<BailCase>) actualRequestEntity.getBody();

        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualContentTypeHeader);
        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualAcceptHeader);
        assertEquals(expectedServiceToken, actualServiceAuthHeader);
        assertEquals(expectedAccessToken, actualAuthorizationHeader);
        assertEquals(actualPostBody, callback);

        assertEquals(notifiedSubmitCallbackResponse.getConfirmationHeader(),
                     actualPostSubmitCallbackResponse.getConfirmationHeader()
        );
        assertEquals(notifiedSubmitCallbackResponse.getConfirmationBody(),
                     actualPostSubmitCallbackResponse.getConfirmationBody()
        );
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegate(null, ENDPOINT)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegate(callback, null)).hasMessage(
            "endpoint must not be null").isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_arguments_for_post_submitted() {

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegatePostSubmit(null, ENDPOINT)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegatePostSubmit(callback, null)).hasMessage(
            "endpoint must not be null").isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void wraps_http_server_exception_when_calling_documents_api() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);
        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(restTemplate.exchange(eq(ENDPOINT),
                                   eq(HttpMethod.POST),
                                   any(HttpEntity.class),
                                   any(ParameterizedTypeReference.class)
        )).thenThrow(underlyingException);

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT)).isExactlyInstanceOf(
            BailCaseServiceResponseException.class).hasMessageContaining("Couldn't delegate callback to API").hasCause(
            underlyingException);
    }

    @Test
    void wraps_http_client_exception_when_calling_documents_api() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);
        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(restTemplate.exchange(eq(ENDPOINT),
                                   eq(HttpMethod.POST),
                                   any(HttpEntity.class),
                                   any(ParameterizedTypeReference.class)
        )).thenThrow(underlyingException);

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT)).isExactlyInstanceOf(
            BailCaseServiceResponseException.class).hasMessageContaining("Couldn't delegate callback to API").hasCause(
            underlyingException);
    }

    @Test
    void wraps_http_client_exception_when_calling_notifications_api() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);
        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(restTemplate
                 .exchange(
                     eq(ENDPOINT),
                     eq(HttpMethod.POST),
                     any(HttpEntity.class),
                     any(ParameterizedTypeReference.class)
                 )).thenThrow(underlyingException);

        assertThatThrownBy(() -> bailCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT))
            .isExactlyInstanceOf(BailCaseServiceResponseException.class)
            .hasMessageContaining("Couldn't delegate callback to API")
            .hasCause(underlyingException);
    }

}
