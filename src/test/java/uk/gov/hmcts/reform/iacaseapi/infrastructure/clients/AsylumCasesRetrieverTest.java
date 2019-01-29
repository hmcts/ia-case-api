package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.UserCredentialsProvider;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class AsylumCasesRetrieverTest {

    @Mock private RestTemplate restTemplate;
    @Mock private AuthTokenGenerator serviceAuthorizationTokenGenerator;
    @Mock private UserCredentialsProvider systemUserCredentialsProvider;

    private final String someServiceAuthorizationToken = "some-service-authorization";
    private final String someIdamAccessToken = "some-access-token";
    private final String ccdBaseUrl = "some-url";
    private final String userId = "10";
    private final String searchPathUrlTemplate = "someSearchPathTemplate";
    private final String searchPathMetadataUrlTemplate = "someSearchPathMetadataTemplate";

    @Mock private ResponseEntity responseEntity;

    @Captor ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReference;
    @Captor ArgumentCaptor<Map<String, String>> urlVariables;
    @Captor ArgumentCaptor<HttpMethod> httpMethod;
    @Captor ArgumentCaptor<HttpEntity> httpEntity;
    @Captor ArgumentCaptor<String> urlCaptor;

    private AsylumCasesRetriever underTest;

    @Before
    public void setUp() {

        underTest = new AsylumCasesRetriever(
            searchPathUrlTemplate,
            searchPathMetadataUrlTemplate,
            ccdBaseUrl,
            restTemplate,
            serviceAuthorizationTokenGenerator,
            systemUserCredentialsProvider
        );

        Mockito.reset(serviceAuthorizationTokenGenerator, systemUserCredentialsProvider);

        when(serviceAuthorizationTokenGenerator.generate()).thenReturn(someServiceAuthorizationToken);
        when(systemUserCredentialsProvider.getAccessToken()).thenReturn(someIdamAccessToken);
        when(systemUserCredentialsProvider.getId()).thenReturn(userId);
    }

    @Test
    public void builds_get_asylum_cases_http_request_correctly() {

        when(restTemplate.exchange(
            urlCaptor.capture(),
            httpMethod.capture(),
            httpEntity.capture(),
            parameterizedTypeReference.capture(),
            urlVariables.capture())).thenReturn(responseEntity);

        underTest.getAsylumCasesPage("1");

        verify(serviceAuthorizationTokenGenerator).generate();
        verify(systemUserCredentialsProvider).getAccessToken();
        verify(systemUserCredentialsProvider).getId();

        assertTrue(urlCaptor.getValue()
            .startsWith(ccdBaseUrl));

        assertThat(urlCaptor.getValue()
            .endsWith("?page=1")).isEqualTo(true);

        assertThat(httpMethod.getValue().equals(GET)).isEqualTo(true);

        assertThat(httpEntity.getValue().getHeaders().get(AUTHORIZATION))
            .containsExactly(someIdamAccessToken);

        assertThat(httpEntity.getValue().getHeaders().get("ServiceAuthorization"))
            .contains(someServiceAuthorizationToken);

        assertThat(urlVariables.getValue().get("uid"))
            .isEqualToIgnoringCase(userId);

        assertThat(urlVariables.getValue().get("jid"))
            .isEqualToIgnoringCase("IA");

        assertThat(urlVariables.getValue().get("ctid"))
            .isEqualToIgnoringCase("Asylum");
    }

    @Test
    public void builds_get_asylum_cases_pagination_metadata_http_request_correctly() {

        when(restTemplate.exchange(
            urlCaptor.capture(),
            httpMethod.capture(),
            httpEntity.capture(),
            parameterizedTypeReference.capture(),
            urlVariables.capture())).thenReturn(responseEntity);

        when(responseEntity.getBody()).thenReturn(singletonMap("total_pages_count", "1"));

        int numberOfPages = underTest.getNumberOfPages();

        verify(serviceAuthorizationTokenGenerator).generate();
        verify(systemUserCredentialsProvider).getAccessToken();
        verify(systemUserCredentialsProvider).getId();

        Assertions.assertThat(numberOfPages).isEqualTo(1);

        assertTrue(urlCaptor.getValue()
            .startsWith(ccdBaseUrl));

        assertThat(urlCaptor.getValue())
            .contains(searchPathMetadataUrlTemplate);

        assertThat(httpMethod.getValue())
            .isEqualTo(GET);

        assertThat(httpEntity.getValue().getHeaders().get(AUTHORIZATION))
            .containsExactly(someIdamAccessToken);

        assertThat(httpEntity.getValue().getHeaders().get("ServiceAuthorization"))
            .containsExactly(someServiceAuthorizationToken);

        assertThat(urlVariables.getValue().get("uid"))
            .isEqualToIgnoringCase(userId);

        assertThat(urlVariables.getValue().get("jid"))
            .isEqualToIgnoringCase("IA");

        assertThat(urlVariables.getValue().get("ctid"))
            .isEqualToIgnoringCase("Asylum");
    }

    @Test
    public void wraps_http_server_exception_when_failed_to_retrieve_asylum_cases_from_ccd() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        when(underlyingException.getMessage()).thenReturn("Some CCD Error");

        when(restTemplate.exchange(
            Mockito.anyString(),
            Mockito.any(HttpMethod.class),
            Mockito.any(HttpEntity.class),
            Mockito.any(ParameterizedTypeReference.class),
            Mockito.any(Map.class)))
            .thenThrow(underlyingException);

        assertThatThrownBy(() -> underTest.getAsylumCasesPage("1"))
            .isExactlyInstanceOf(CoreCaseDataAccessException.class)
            .hasFieldOrPropertyWithValue("alertLevel", AlertLevel.P2)
            .hasMessageContaining("Couldn't retrieve asylum cases from CCD: Some CCD Error");
    }

    @Test
    public void wraps_http_client_exception_when_failed_to_retrieve_asylum_cases_from_ccd() {

        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        when(underlyingException.getMessage()).thenReturn("Some CCD Error");

        when(restTemplate.exchange(
            Mockito.anyString(),
            Mockito.any(HttpMethod.class),
            Mockito.any(HttpEntity.class),
            Mockito.any(ParameterizedTypeReference.class),
            Mockito.any(Map.class)))
            .thenThrow(underlyingException);

        assertThatThrownBy(() -> underTest.getAsylumCasesPage("1"))
            .isExactlyInstanceOf(CoreCaseDataAccessException.class)
            .hasFieldOrPropertyWithValue("alertLevel", AlertLevel.P2)
            .hasMessageContaining("Couldn't retrieve asylum cases from CCD: Some CCD Error");
    }
}
