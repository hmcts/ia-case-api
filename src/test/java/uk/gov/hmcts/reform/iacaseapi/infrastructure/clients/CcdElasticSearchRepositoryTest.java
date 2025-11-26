package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdSearchResult;

@ExtendWith(MockitoExtension.class)
class CcdElasticSearchRepositoryTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private UserDetails userDetails;
    @Mock
    private ResponseEntity<CcdSearchResult> responseEntity;

    private CcdElasticSearchRepository repository;
    private static final String CCD_URL = "http://localhost:4452";
    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "service-token";

    @BeforeEach
    void setUp() {
        repository = new CcdElasticSearchRepository(
            restTemplate,
            serviceAuthTokenGenerator,
            userDetails,
            CCD_URL
        );

        when(userDetails.getAccessToken()).thenReturn(USER_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void should_execute_search_successfully() {
        CcdSearchQuery query = new CcdSearchQuery(
            new HashMap<>(),
            10,
            List.of("reference")
        );

        CcdSearchResult expectedResult = new CcdSearchResult(1, Collections.emptyList());
        when(responseEntity.getBody()).thenReturn(expectedResult);
        when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CcdSearchResult.class)
        )).thenReturn(responseEntity);

        CcdSearchResult result = repository.searchCases(query);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(restTemplate).exchange(
            any(String.class),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CcdSearchResult.class)
        );
    }

    @Test
    void should_throw_exception_when_rest_call_fails() {
        CcdSearchQuery query = new CcdSearchQuery(
            new HashMap<>(),
            10,
            List.of("reference")
        );

        when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CcdSearchResult.class)
        )).thenThrow(new RestClientException("Connection failed"));

        assertThrows(
            CcdElasticSearchRepository.CcdSearchException.class,
            () -> repository.searchCases(query)
        );
    }

    @Test
    void should_use_correct_headers() {
        CcdSearchQuery query = new CcdSearchQuery(
            new HashMap<>(),
            10,
            List.of("reference")
        );

        CcdSearchResult expectedResult = new CcdSearchResult(0, Collections.emptyList());
        when(responseEntity.getBody()).thenReturn(expectedResult);
        when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CcdSearchResult.class)
        )).thenReturn(responseEntity);

        repository.searchCases(query);

        verify(userDetails).getAccessToken();
        verify(serviceAuthTokenGenerator).generate();
    }
}

