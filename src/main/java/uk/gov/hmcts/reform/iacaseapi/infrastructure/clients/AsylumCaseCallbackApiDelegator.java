package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@Service
@Slf4j
public class AsylumCaseCallbackApiDelegator {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;

    public AsylumCaseCallbackApiDelegator(
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") AccessTokenProvider accessTokenProvider,
        RestTemplate restTemplate
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.restTemplate = restTemplate;
    }

    public AsylumCase delegate(
        Callback<AsylumCase> callback,
        String endpoint
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(endpoint, "endpoint must not be null");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = accessTokenProvider.getAccessToken();

        HttpEntity<Callback<AsylumCase>> requestEntity = new HttpEntity<>(callback, setHeaders(serviceAuthorizationToken,accessToken));

        try {

            ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> response = restTemplate
                    .exchange(
                            endpoint,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
                            }
                    );

            log.info("-----------contentLength1: {}", response.getHeaders().getContentLength());
            response.getHeaders().forEach((k, v) -> log.info("-----{} = {}", k, v));
            simError("111");

            return Optional
                .of(response
                )
                .map(ResponseEntity::getBody)
                .map(PreSubmitCallbackResponse::getData)
                .orElse(new AsylumCase());

        } catch (RestClientException e) {

            throw new AsylumCaseServiceResponseException(
                "Couldn't delegate callback to API: " + endpoint,
                e
            );
        }
    }

    public PostSubmitCallbackResponse delegatePostSubmit(
        Callback<AsylumCase> callback,
        String endpoint
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(endpoint, "endpoint must not be null");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = accessTokenProvider.getAccessToken();

        HttpEntity<Callback<AsylumCase>> requestEntity = new HttpEntity<>(callback, setHeaders(serviceAuthorizationToken,accessToken));

        try {

            ResponseEntity<PostSubmitCallbackResponse> response = restTemplate
                    .exchange(
                            endpoint,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PostSubmitCallbackResponse>() {
                            }
                    );

            log.info("-----------contentLength2: {}", response.getHeaders().getContentLength());
            response.getHeaders().forEach((k, v) -> log.info("-----{} = {}", k, v));
            simError("222");

            return Optional
                .of(response
                )
                .map(ResponseEntity::getBody)
                .orElse(new PostSubmitCallbackResponse());

        } catch (RestClientException e) {

            throw new AsylumCaseServiceResponseException(
                "Couldn't delegate callback to API: " + endpoint,
                e
            );
        }
    }

    private void simError(String number) {
        try {
            throw new RuntimeException(number);
        } catch (RuntimeException ex) {
            log.error(ex.getMessage(), ex);
        }
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
