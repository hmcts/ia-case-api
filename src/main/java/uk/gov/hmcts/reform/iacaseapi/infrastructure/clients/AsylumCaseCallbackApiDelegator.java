package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
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

        AsylumCase asylumCase0 = callback.getCaseDetails().getCaseData();
        log.info("----------AsylumCaseCallbackApiDelegator111 {}", endpoint);
        Optional<AppealType> appealType0Opt = asylumCase0.read(APPEAL_TYPE, AppealType.class);
        log.info("{}", appealType0Opt);
        log.info("----------AsylumCaseCallbackApiDelegator222 {}", endpoint);

        try {

            ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> response = restTemplate
                    .exchange(
                            endpoint,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
                            }
                    );

            log.info("----------contentLength {}", response.getHeaders().getContentLength());
            response.getHeaders().forEach((k, v) -> log.info("-----header: {} => {}", k, v));
            log.info("----------AsylumCaseCallbackApiDelegator333000 res.getBody() == null: {}", response.getBody() == null);
            if (response.getBody() != null) {
                log.info(
                    "----------AsylumCaseCallbackApiDelegator333000 res.getBody().getData() == null: {}",
                    response.getBody().getData() == null
                );
            }

            AsylumCase asylumCase = Optional.of(response)
                    .map(ResponseEntity::getBody)
                    .map(PreSubmitCallbackResponse::getData)
                    .orElse(new AsylumCase());
            log.info("----------AsylumCaseCallbackApiDelegator333");
            Optional<AppealType> appealTypeOpt = asylumCase.read(APPEAL_TYPE, AppealType.class);
            log.info("{}", appealTypeOpt);
            log.info("{}", asylumCase);
            log.info("----------AsylumCaseCallbackApiDelegator444");
            return asylumCase;

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

        log.info("--------------------444 {}", endpoint);
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

            throw new AsylumCaseServiceResponseException(
                    "Couldn't delegate callback to API: " + endpoint,
                    e
            );
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
