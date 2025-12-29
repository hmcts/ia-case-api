package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;

@Service
public class BailCaseCallbackApiDelegator {
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final Logger LOG = getLogger(BailCaseCallbackApiDelegator.class);

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;
    private final String secret;
    private final String microService;
    private final String authUrl;

    public BailCaseCallbackApiDelegator(
        AuthTokenGenerator serviceAuthTokenGenerator,
        AccessTokenProvider accessTokenProvider,
        RestTemplate restTemplate,
        @Value("${idam.s2s-auth.totp_secret}") String secret,
        @Value("${idam.s2s-auth.microservice}") String microService,
        @Value("${idam.s2s-auth.url}") String authUrl
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.restTemplate = restTemplate;
        this.secret = secret;
        this.microService = microService;
        this.authUrl = authUrl;
    }

    public BailCase delegate(
        Callback<BailCase> callback,
        String endpoint
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(endpoint, "endpoint must not be null");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = accessTokenProvider.getAccessToken();

        HttpEntity<Callback<BailCase>> requestEntity = new HttpEntity<>(
            callback, setHeaders(serviceAuthorizationToken, accessToken)
        );

        try {
            return Optional
                .of(restTemplate
                        .exchange(
                            endpoint,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PreSubmitCallbackResponse<BailCase>>() {
                            }
                        )
                )
                .map(ResponseEntity::getBody)
                .map(PreSubmitCallbackResponse::getData)
                .orElse(new BailCase());
        } catch (RestClientException e) {
            throw new BailCaseServiceResponseException(
                "Couldn't delegate callback to API: " + endpoint,
                e
            );
        }
    }

    public PostSubmitCallbackResponse delegatePostSubmit(
        Callback<BailCase> callback,
        String endpoint
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(endpoint, "endpoint must not be null");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = accessTokenProvider.getAccessToken();

        HttpEntity<Callback<BailCase>> requestEntity = new HttpEntity<>(callback,
                                                                        setHeaders(
                                                                            serviceAuthorizationToken,
                                                                            accessToken
                                                                        )
        );

        try {
            return Optional
                .of(restTemplate
                        .exchange(
                            endpoint,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PostSubmitCallbackResponse>() {
                            }
                        )
                )
                .map(ResponseEntity::getBody)
                .orElse(new PostSubmitCallbackResponse());
        } catch (RestClientException e) {
            throw new BailCaseServiceResponseException(
                "Couldn't delegate callback to API: " + endpoint,
                e
            );
        }
    }

    private HttpHeaders setHeaders(String authorizationToken, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, authorizationToken);
        return headers;
    }
}
