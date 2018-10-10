package uk.gov.hmcts.reform.iacaseapi.forms.domain.api;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.UserProvider;

@Service
public class CcdAsylumCaseFetcher {

    private static final String PATH_TEMPLATE = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final String ccdBaseUrl;
    private final RestTemplate restTemplate;
    private final AccessTokenProvider accessTokenProvider;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserProvider userProvider;

    public CcdAsylumCaseFetcher(
        @Value("${ccdApi.baseUrl}") String ccdBaseUrl,
        @Autowired RestTemplate restTemplate,
        @Autowired AccessTokenProvider accessTokenProvider,
        @Autowired AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Autowired UserProvider userProvider
    ) {
        this.ccdBaseUrl = ccdBaseUrl;
        this.restTemplate = restTemplate;
        this.accessTokenProvider = accessTokenProvider;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userProvider = userProvider;
    }

    public CaseDetails<AsylumCase> fetch(
        String caseId
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, accessTokenProvider.getAccessToken());
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationTokenGenerator.generate());
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

        HttpEntity<?> request =
            new HttpEntity<>(headers);

        return restTemplate
            .exchange(
                ccdBaseUrl + PATH_TEMPLATE,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<CaseDetails<AsylumCase>>() {
                },
                ImmutableMap.of(
                    "uid", userProvider.getUserId(),
                    "jid", "IA",
                    "ctid", "Asylum",
                    "cid", caseId
                )
            )
            .getBody();
    }
}
