package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.UNKNOWN;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberInitializerException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.UserCredentialsProvider;

@Service
class AsylumCasesRetriever {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final String caseworkerAsylumCaseSearchUrlTemplate;
    private final String caseworkerAsylumCaseSearchMetadataUrlTemplate;
    private final String ccdBaseUrl;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserCredentialsProvider systemUserCredentialsProvider;

    public AsylumCasesRetriever(
        @Value("${core_case_data_api_url_template}") String caseworkerAsylumCaseSearchUrlTemplate,
        @Value("${core_case_data_api_metatdata_url}") String caseworkerAsylumCaseSearchMetadataUrlTemplate,
        @Value("${core_case_data_api_url}") String ccdBaseUrl,
        RestTemplate restTemplate,
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Qualifier("systemUser") UserCredentialsProvider systemUserCredentialsProvider
    ) {
        this.caseworkerAsylumCaseSearchUrlTemplate = caseworkerAsylumCaseSearchUrlTemplate;
        this.caseworkerAsylumCaseSearchMetadataUrlTemplate = caseworkerAsylumCaseSearchMetadataUrlTemplate;
        this.ccdBaseUrl = ccdBaseUrl;
        this.restTemplate = restTemplate;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.systemUserCredentialsProvider = systemUserCredentialsProvider;
    }

    @Retryable(
        value = {AppealReferenceNumberInitializerException.class},
        backoff = @Backoff(delay = 5000))
    public List<Map> getAsylumCasesPage(String pageNumber) {

        String accessToken = systemUserCredentialsProvider.getAccessToken();

        List<Map> asylumCaseDetails;

        try {

            asylumCaseDetails = restTemplate
                .exchange(
                    ccdBaseUrl + caseworkerAsylumCaseSearchUrlTemplate + getQueryStringForStates() + "&page=" + pageNumber,
                    HttpMethod.GET,
                    new HttpEntity<>(getHttpHeaders(accessToken, serviceAuthorizationTokenGenerator.generate())),
                    new ParameterizedTypeReference<List<Map>>() {
                    },
                    ImmutableMap.of(
                        "uid", systemUserCredentialsProvider.getId(),
                        "jid", "IA",
                        "ctid", "Asylum"
                    )
                ).getBody();

        } catch (RestClientException | NullPointerException exp) {
            throw new AppealReferenceNumberInitializerException("Couldn't retrieve asylum cases from CCD", exp);
        }

        return asylumCaseDetails;
    }

    @Retryable(
        value = {AppealReferenceNumberInitializerException.class},
        backoff = @Backoff(delay = 5000))
    public int getNumberOfPages() {

        String accessToken = systemUserCredentialsProvider.getAccessToken();

        int numberOfPages;

        try {
            Map<String, String> paginationMetadata = restTemplate
                .exchange(
                    ccdBaseUrl + caseworkerAsylumCaseSearchMetadataUrlTemplate + getQueryStringForStates(),
                    HttpMethod.GET,
                    new HttpEntity<>(getHttpHeaders(accessToken, serviceAuthorizationTokenGenerator.generate())),
                    new ParameterizedTypeReference<Map<String, String>>() {
                    },
                    ImmutableMap.of(
                        "uid", systemUserCredentialsProvider.getId(),
                        "jid", "IA",
                        "ctid", "Asylum"
                    )
                ).getBody();

            numberOfPages = Integer.valueOf(paginationMetadata.get("total_pages_count"));

        } catch (RestClientException | NullPointerException exp) {
            throw new AppealReferenceNumberInitializerException("Couldn't retrieve metadata from CCD", exp);
        }

        return numberOfPages;
    }

    private HttpHeaders getHttpHeaders(String accessToken, String serviceAuthorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorization);
        headers.set(ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        return headers;
    }

    private String getQueryStringForStates() {
        return "?" + stream(State.values())
            .filter(state -> !state.equals(APPEAL_STARTED))
            .filter(state -> !state.equals(UNKNOWN))
            .map(state -> "state=" + state)
            .collect(joining("&"));
    }
}
