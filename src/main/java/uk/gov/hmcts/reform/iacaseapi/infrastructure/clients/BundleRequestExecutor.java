package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.DocumentServiceResponseException;

@Slf4j
@Service
public class BundleRequestExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;

    public BundleRequestExecutor(RestTemplate restTemplate, AuthTokenGenerator serviceAuthTokenGenerator, UserDetails userDetails) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
    }

    public PreSubmitCallbackResponse<AsylumCase> post(
        final Callback<AsylumCase> payload,
        final String endpoint
    ) {

        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String accessToken = userDetails.getAccessToken();
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<Callback<AsylumCase>> requestEntity = new HttpEntity<>(payload, headers);

        PreSubmitCallbackResponse<AsylumCase> response;

        try {
            response =
                restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
                        }
                    ).getBody();

        } catch (RestClientResponseException e) {

            throw new DocumentServiceResponseException(
                "Couldn't create bundle using API: " + endpoint,
                e
            );
        }

        return response;

    }

}
