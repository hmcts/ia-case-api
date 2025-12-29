package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.CaseTypeHelper.isAsylumCase;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@Service
public class CallbackApiDelegator {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;

    public CallbackApiDelegator(
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") AccessTokenProvider accessTokenProvider,
        RestTemplate restTemplate
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public  <T extends CaseData> T delegate(
        Callback<T> callback,
        String endpoint
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(endpoint, "endpoint must not be null");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = accessTokenProvider.getAccessToken();
        CaseData caseData = callback.getCaseDetails().getCaseData();
        HttpEntity<Callback<T>> requestEntity = new HttpEntity<>(callback, setHeaders(serviceAuthorizationToken, accessToken));

        try {

            return Optional
                .of(restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<T>>() {
                        }
                    )
                )
                .map(ResponseEntity::getBody)
                .map(PreSubmitCallbackResponse::getData)
                .orElse(isAsylumCase(caseData) ? (T) new AsylumCase() : (T) new BailCase());

        } catch (RestClientException e) {

            throw new ServiceResponseException(
                "Couldn't delegate callback to API: " + endpoint,
                e
            );
        }
    }

    public <T extends CaseData> PostSubmitCallbackResponse delegatePostSubmit(
        Callback<T> callback,
        String endpoint
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(endpoint, "endpoint must not be null");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = accessTokenProvider.getAccessToken();

        HttpEntity<Callback<T>> requestEntity = new HttpEntity<>(callback, setHeaders(serviceAuthorizationToken, accessToken));

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

            throw new ServiceResponseException(
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
