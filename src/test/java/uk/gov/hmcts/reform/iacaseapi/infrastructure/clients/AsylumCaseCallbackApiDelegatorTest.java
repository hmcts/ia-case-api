package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumCaseCallbackApiDelegatorTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String ENDPOINT = "http://endpoint";

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private AccessTokenProvider accessTokenProvider;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock
    private PostSubmitCallbackResponse postSubmitCallbackResponse;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;

    @BeforeEach
    public void setUp() {
        asylumCaseCallbackApiDelegator =
            new AsylumCaseCallbackApiDelegator(
                serviceAuthTokenGenerator,
                accessTokenProvider,
                restTemplate
            );
    }

    @Test
    void should_call_document_api_to_generate_document() {
        // Setup callback mocks for this test
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(12345L);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(callbackResponse.getData()).thenReturn(notifiedAsylumCase);
        doReturn(new ResponseEntity<>(callbackResponse, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(ENDPOINT),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        final AsylumCase actualAsylumCase = asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT);

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(ENDPOINT),
            eq(HttpMethod.POST),
            requestEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity actualRequestEntity = requestEntityCaptor.getAllValues().get(0);

        final String actualContentTypeHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualServiceAuthorizationHeader =
            actualRequestEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final String actualAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final Callback<AsylumCase> actualPostBody = (Callback<AsylumCase>) actualRequestEntity.getBody();

        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualContentTypeHeader);
        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualAcceptHeader);
        assertEquals(expectedServiceToken, actualServiceAuthorizationHeader);
        assertEquals(expectedAccessToken, actualAuthorizationHeader);
        assertEquals(actualPostBody, callback);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    void should_call_notifications_api_to_send_notifications_in_ccd_submitted() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final PostSubmitCallbackResponse notifiedSubmitCallbackResponse = mock(PostSubmitCallbackResponse.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        doReturn(new ResponseEntity<>(postSubmitCallbackResponse, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(ENDPOINT),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        final PostSubmitCallbackResponse actualPostSubmitCallbackResponse = asylumCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT);

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(ENDPOINT),
            eq(HttpMethod.POST),
            requestEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity actualRequestEntity = requestEntityCaptor.getAllValues().get(0);

        final String actualContentTypeHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualServiceAuthorizationHeader =
            actualRequestEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final String actualAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        final Callback<AsylumCase> actualPostBody = (Callback<AsylumCase>) actualRequestEntity.getBody();

        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualContentTypeHeader);
        assertEquals(MediaType.APPLICATION_JSON_VALUE, actualAcceptHeader);
        assertEquals(expectedServiceToken, actualServiceAuthorizationHeader);
        assertEquals(expectedAccessToken, actualAuthorizationHeader);
        assertEquals(actualPostBody, callback);

        assertEquals(notifiedSubmitCallbackResponse.getConfirmationHeader(), actualPostSubmitCallbackResponse.getConfirmationHeader());
        assertEquals(notifiedSubmitCallbackResponse.getConfirmationBody(), actualPostSubmitCallbackResponse.getConfirmationBody());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegate(null, ENDPOINT))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegate(callback, null))
            .hasMessage("endpoint must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_arguments_for_post_submitted() {

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegatePostSubmit(null, ENDPOINT))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegatePostSubmit(callback, null))
            .hasMessage("endpoint must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void wraps_http_server_exception_when_calling_documents_api() {
        // Setup callback mocks for this test
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(12345L);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);
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

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class)
            .hasMessageContaining("Couldn't delegate callback to API")
            .hasCause(underlyingException);
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

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class)
            .hasMessageContaining("Couldn't delegate callback to API")
            .hasCause(underlyingException);
    }

    @Test
    void wraps_http_client_exception_when_calling_documents_api() {
        // Setup callback mocks for this test
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(12345L);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);

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

        assertThatThrownBy(() -> asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class)
            .hasMessageContaining("Couldn't delegate callback to API")
            .hasCause(underlyingException);
    }
}
