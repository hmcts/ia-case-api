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
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.UserCredentialsProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AsylumCaseNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String ABOUT_TO_SUBMIT_PATH = "/path";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserCredentialsProvider requestUserCredentialsProvider;
    @Mock private RestTemplate restTemplate;
    @Mock private Callback<AsylumCase> callback;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private AsylumCaseNotificationApiSender asylumCaseNotificationApiSender;

    @Before
    public void setUp() {

        asylumCaseNotificationApiSender =
            new AsylumCaseNotificationApiSender(
                serviceAuthTokenGenerator,
                requestUserCredentialsProvider,
                restTemplate,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH
            );
    }

    @Test
    public void should_call_notification_api_to_send_notification() {

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

        final AsylumCase actualAsylumCase = asylumCaseNotificationApiSender.send(callback);

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

        assertThatThrownBy(() -> asylumCaseNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
