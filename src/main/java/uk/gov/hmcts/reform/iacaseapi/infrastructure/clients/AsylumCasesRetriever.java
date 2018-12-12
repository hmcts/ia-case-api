package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.UNKNOWN;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.AsylumCaseRetrievalException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenDecoder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.IdamAuthorizor;

@Service
class AsylumCasesRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(AsylumCasesRetriever.class);

    private static final String PATH_TEMPLATE = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases";
    private static final String PAGINATION_PATH_TEMPLATE = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/pagination_metadata";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final String ccdBaseUrl;
    private final String iaSystemUser;
    private final String iaSystemPassword;
    private final RestTemplate restTemplate;
    private final IdamAuthorizor idamAuthorizor;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final AccessTokenDecoder accessTokenDecoder;

    public AsylumCasesRetriever(
            @Value("${core_case_data_api_url}") String ccdBaseUrl,
            @Value("${ia_system_user}") String iaSystemUser,
            @Value("${ia_system_user_password}") String iaSystemPassword,
            RestTemplate restTemplate,
            IdamAuthorizor idamAuthorizor,
            AuthTokenGenerator serviceAuthorizationTokenGenerator,
            AccessTokenDecoder accessTokenDecoder
    ) {
        this.ccdBaseUrl = ccdBaseUrl;
        this.iaSystemUser = iaSystemUser;
        this.iaSystemPassword = iaSystemPassword;
        this.restTemplate = restTemplate;
        this.idamAuthorizor = idamAuthorizor;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.accessTokenDecoder = accessTokenDecoder;
    }

    @Retryable(
            value = {AsylumCaseRetrievalException.class},
            backoff = @Backoff(delay = 5000))
    public List<Map> getAsylumCasesPage(String pageNumber) {

        String accessToken = idamAuthorizor.exchangeForAccessToken(
                iaSystemUser,
                iaSystemPassword);

        List<Map> asylumCaseDetails;

        try {

            asylumCaseDetails = restTemplate
                    .exchange(
                            ccdBaseUrl + PATH_TEMPLATE + getQueryStringForStates() + "&page=" + pageNumber,
                            HttpMethod.GET,
                            new HttpEntity<>(getHttpHeaders(accessToken, serviceAuthorizationTokenGenerator.generate())),
                            new ParameterizedTypeReference<List<Map>>() {
                            },
                            ImmutableMap.of(
                                    "uid", accessTokenDecoder.decode(accessToken).get("id"),
                                    "jid", "IA",
                                    "ctid", "Asylum"
                            )
                    ).getBody();

        } catch (RestClientException | NullPointerException exp) {
            throw new AsylumCaseRetrievalException("Couldn't retrieve asylum cases from CCD", exp);
        }

        return asylumCaseDetails;
    }

    @Retryable(
            value = {AsylumCaseRetrievalException.class},
            backoff = @Backoff(delay = 5000))
    public int getNumberOfPages() {

        String accessToken = idamAuthorizor.exchangeForAccessToken(
                iaSystemUser,
                iaSystemPassword);

        int numberOfPages;

        try {
            Map<String, String> paginationMetadata = restTemplate
                    .exchange(
                            ccdBaseUrl + PAGINATION_PATH_TEMPLATE + getQueryStringForStates(),
                            HttpMethod.GET,
                            new HttpEntity<>(getHttpHeaders(accessToken, serviceAuthorizationTokenGenerator.generate())),
                            new ParameterizedTypeReference<Map<String, String>>() {
                            },
                            ImmutableMap.of(
                                    "uid", accessTokenDecoder.decode(accessToken).get("id"),
                                    "jid", "IA",
                                    "ctid", "Asylum"
                            )
                    ).getBody();

            numberOfPages = Integer.valueOf(paginationMetadata.get("total_pages_count"));

        } catch (RestClientException | NullPointerException exp) {
            throw new AsylumCaseRetrievalException("Couldn't retrieve metadata from CCD", exp);
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
