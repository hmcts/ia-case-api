package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.UserCredentialsProvider;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AsylumCaseDocumentApiGeneratorTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String ABOUT_TO_SUBMIT_PATH = "/path";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserCredentialsProvider requestUserCredentialsProvider;
    @Mock private RestTemplate restTemplate;
    @Mock private Callback<AsylumCase> callback;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private AsylumCaseDocumentApiGenerator asylumCaseDocumentApiGenerator;

    @Before
    public void setUp() {

        asylumCaseDocumentApiGenerator =
            new AsylumCaseDocumentApiGenerator(
                serviceAuthTokenGenerator,
                requestUserCredentialsProvider,
                restTemplate,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH
            );
    }

    @Test
    public void should_call_document_api_to_generate_document() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(requestUserCredentialsProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(callbackResponse.getData()).thenReturn(notifiedAsylumCase);
        doReturn(new ResponseEntity<>(callbackResponse, HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                eq(ENDPOINT + ABOUT_TO_SUBMIT_PATH),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        final AsylumCase actualAsylumCase = asylumCaseDocumentApiGenerator.generate(callback);

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(
            eq(ENDPOINT + ABOUT_TO_SUBMIT_PATH),
            eq(HttpMethod.POST),
            requestEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity actualRequestEntity = requestEntityCaptor.getAllValues().get(0);

        final String actualContentTypeHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualServiceAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final String actualAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final Callback<AsylumCase> actualPostBody = (Callback<AsylumCase>) actualRequestEntity.getBody();

        assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, actualContentTypeHeader);
        assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, actualAcceptHeader);
        assertEquals(expectedServiceToken, actualServiceAuthorizationHeader);
        assertEquals(expectedAccessToken, actualAuthorizationHeader);
        assertEquals(actualPostBody, callback);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> asylumCaseDocumentApiGenerator.generate(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void wraps_http_server_exception_when_calling_documents_api() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);
        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(requestUserCredentialsProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(restTemplate
            .exchange(
                eq(ENDPOINT + ABOUT_TO_SUBMIT_PATH),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> asylumCaseDocumentApiGenerator.generate(callback))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class)
            .hasFieldOrPropertyWithValue("alertLevel", AlertLevel.P2)
            .hasMessageContaining("Couldn't generate asylum case documents")
            .hasCause(underlyingException);
    }

    @Test
    public void wraps_http_client_exception_when_calling_documents_api() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);
        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(requestUserCredentialsProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(restTemplate
            .exchange(
                eq(ENDPOINT + ABOUT_TO_SUBMIT_PATH),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> asylumCaseDocumentApiGenerator.generate(callback))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class)
            .hasFieldOrPropertyWithValue("alertLevel", AlertLevel.P2)
            .hasMessageContaining("Couldn't generate asylum case documents")
            .hasCause(underlyingException);
    }
}
