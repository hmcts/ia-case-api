package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.UserCredentialsProvider;

@Service
public class AsylumCaseNotificationApiSender implements NotificationSender<AsylumCase> {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserCredentialsProvider requestUserCredentialsProvider;
    private final RestTemplate restTemplate;
    private final String endpoint;
    private final String aboutToSubmitPath;

    public AsylumCaseNotificationApiSender(
        AuthTokenGenerator serviceAuthTokenGenerator,
        UserCredentialsProvider requestUserCredentialsProvider,
        RestTemplate restTemplate,
        @Value("${notificationsApi.endpoint}") String endpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.requestUserCredentialsProvider = requestUserCredentialsProvider;
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public AsylumCase send(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        String userAccessToken = requestUserCredentialsProvider.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);
        headers.set(HttpHeaders.AUTHORIZATION, userAccessToken);

        HttpEntity<Callback<AsylumCase>> requestEntity = new HttpEntity<>(callback, headers);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse;

        try {
            callbackResponse = restTemplate
                .exchange(
                    endpoint + aboutToSubmitPath,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
                    }
                ).getBody();


        } catch (RestClientException clientEx) {
            throw new AsylumCaseServiceResponseException(
                "Couldn't send asylum case notifications with notifications api",
                clientEx
            );

        }

        return callbackResponse.getData();
    }
}
