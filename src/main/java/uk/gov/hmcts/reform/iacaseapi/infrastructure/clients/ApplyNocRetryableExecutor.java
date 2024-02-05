package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ApplyNocRetryableExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;
    private final String aacUrl;
    private final String applyNocAssignmentsApiPath;

    public ApplyNocRetryableExecutor(
        RestTemplate restTemplate,
        AuthTokenGenerator serviceAuthTokenGenerator,
        UserDetailsProvider userDetailsProvider,
        @Value("${assign_case_access_api_url}") String aacUrl,
        @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
        this.aacUrl = aacUrl;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
    }

    @Retryable(maxAttempts = 4, backoff = @Backoff(60000))
    public void retryApplyNoc(final Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        log.info(
            "Executing Apply NoC for case {}",
            callback.getCaseDetails().getId()
        );

        /*if (RetrySynchronizationManager.getContext().getRetryCount() < 2) {
            throw new RestClientResponseException("", 0, "", null, null, null);
        }*/

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        HttpEntity<Callback<AsylumCase>> requestEntity =
            new HttpEntity<>(
                callback,
                setHeaders(serviceAuthorizationToken, accessToken)
            );

        ResponseEntity<Object> response;
        try {
            response = restTemplate
                .exchange(
                    aacUrl + applyNocAssignmentsApiPath,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );
        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Couldn't apply noc AAC case assignment for case ["
                    + callback.getCaseDetails().getId()
                    + "] using API: "
                    + aacUrl + applyNocAssignmentsApiPath,
                e
            );
        }

        log.info("Apply NoC. Http status received from AAC API; {} for case {}",
                response.getStatusCodeValue(), callback.getCaseDetails().getId());
    }

    private HttpHeaders setHeaders(String serviceAuthorizationToken, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);
        return headers;
    }
}
