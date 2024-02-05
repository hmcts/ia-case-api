package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryContext;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.RetryCounter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyNocRetryableExecutorTest {
    private ApplyNocRetryableExecutor applyNocRetryableExecutor;

    private static final String SERVICE_TOKEN = "ABCDEF";
    private static final String ACCESS_TOKEN = "12345";
    private final String aacUrl = "some-aac-host";
    private final String nocAssignmentsApiPath = "some-noc-path";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<Object> responseEntity;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private RetryCounter retryCounter;

    @Mock
    private RetryContext retryContext;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        applyNocRetryableExecutor = new ApplyNocRetryableExecutor(
            restTemplate,
            serviceAuthTokenGenerator,
            userDetailsProvider,
            retryCounter,
            aacUrl,
            nocAssignmentsApiPath
        );
    }

    @Test
    void should_send_post_to_retry_apply_noc_and_receive_201() {

        when(retryCounter.getContext()).thenReturn(retryContext);
        when(retryContext.getRetryCount()).thenReturn(1);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenReturn(responseEntity);

        when(responseEntity.getStatusCodeValue()).thenReturn(HttpStatus.CREATED.value());

        applyNocRetryableExecutor.retryApplyNoc(callback);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_retry_apply_noc() {
        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);

        when(retryCounter.getContext()).thenReturn(retryContext);
        when(retryContext.getRetryCount()).thenReturn(1);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);

        when(restTemplate
            .exchange(
                eq(aacUrl + nocAssignmentsApiPath),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            )
        ).thenThrow(restClientResponseEx);

        assertThatThrownBy(() -> applyNocRetryableExecutor.retryApplyNoc(callback))
            .isInstanceOf(CcdDataIntegrationException.class)
            .hasMessage("Couldn't apply noc AAC case assignment for case ["
                + caseDetails.getId()
                + "] using API: "
                + aacUrl + nocAssignmentsApiPath)
            .hasCauseInstanceOf(RestClientResponseException.class);

        verify(restTemplate)
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
            );
    }
}
