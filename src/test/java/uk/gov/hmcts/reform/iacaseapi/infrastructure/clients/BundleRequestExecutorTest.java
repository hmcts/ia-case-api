package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.DocumentServiceResponseException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BundleRequestExecutorTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String ENDPOINT = "http://endpoint";
    private static final String SERVICE_TOKEN = secure().nextAlphabetic(32);
    private static final String ACCESS_TOKEN = secure().nextAlphabetic(32);

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private RestTemplate restTemplate;

    @Mock private UserDetails userDetails;
    @Mock private Callback<AsylumCase> callback;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> responseEntity;


    private BundleRequestExecutor bundleRequestExecutor;

    @BeforeEach
    public void setUp() {
        bundleRequestExecutor = new BundleRequestExecutor(
            restTemplate,
            serviceAuthTokenGenerator,
            userDetails
        );

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
    }

    @Test
    void should_invoke_endpoint_with_given_payload_and_return_200_with_no_errors() {

        when(restTemplate
            .exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(callbackResponse);

        PreSubmitCallbackResponse<AsylumCase> response =
            bundleRequestExecutor.post(
                callback,
                ENDPOINT

            );

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(callbackResponse);

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
            eq(ENDPOINT),
            eq(HttpMethod.POST),
            requestEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity actualRequestEntity = requestEntityCaptor.getValue();

        final String actualContentTypeHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualServiceAuthorizationHeader =
            actualRequestEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final String actualAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final Callback<AsylumCase> actualPostBody = (Callback<AsylumCase>) actualRequestEntity.getBody();

        assertThat(actualContentTypeHeader).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(actualAcceptHeader).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(actualServiceAuthorizationHeader).isEqualTo(SERVICE_TOKEN);
        assertThat(actualAuthorizationHeader).isEqualTo(ACCESS_TOKEN);
        assertThat(actualPostBody).isEqualTo(callback);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> bundleRequestExecutor.post(null, ENDPOINT))
            .hasMessage("payload must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bundleRequestExecutor.post(callback, null))
            .hasMessage("endpoint must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_handle_http_server_exception_when_calling_api() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        when(restTemplate
            .exchange(
                eq(ENDPOINT),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> bundleRequestExecutor.post(callback, ENDPOINT))
            .isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessageContaining("Couldn't create bundle using API")
            .hasCause(underlyingException);

    }

    @Test
    void should_handle_http_client_exception_when_calling_api() {
        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        when(restTemplate
            .exchange(
                eq(ENDPOINT),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> bundleRequestExecutor.post(callback, ENDPOINT))
            .isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessageContaining("Couldn't create bundle using API")
            .hasCause(underlyingException);
    }


}
