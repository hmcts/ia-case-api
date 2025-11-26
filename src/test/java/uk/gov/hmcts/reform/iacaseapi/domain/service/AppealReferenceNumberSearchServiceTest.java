package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchRepository;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSearchQuery;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdSearchResult;

@ExtendWith(MockitoExtension.class)
class AppealReferenceNumberSearchServiceTest {

    @Mock
    private CcdElasticSearchQueryBuilder queryBuilder;
    @Mock
    private CcdElasticSearchRepository searchRepository;
    @Mock
    private CcdSearchQuery query;

    private AppealReferenceNumberSearchService service;

    @BeforeEach
    void setUp() {
        service = new AppealReferenceNumberSearchService(queryBuilder, searchRepository);
    }

    @Test
    void should_return_true_when_appeal_reference_number_exists() {
        String appealReferenceNumber = "PA/12345/2023";
        CcdSearchResult result = new CcdSearchResult(1, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber);

        assertTrue(exists);
        verify(queryBuilder).buildAppealReferenceNumberQuery(appealReferenceNumber);
        verify(searchRepository).searchCases(query);
    }

    @Test
    void should_return_false_when_appeal_reference_number_does_not_exist() {
        String appealReferenceNumber = "PA/12345/2023";
        CcdSearchResult result = new CcdSearchResult(0, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_appeal_reference_number_is_null() {
        boolean exists = service.appealReferenceNumberExists(null);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_appeal_reference_number_is_empty() {
        boolean exists = service.appealReferenceNumberExists("");

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_search_result_is_null() {
        String appealReferenceNumber = "PA/12345/2023";

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(null);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_search_throws_exception() {
        String appealReferenceNumber = "PA/12345/2023";

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(any())).thenThrow(
            new CcdElasticSearchRepository.CcdSearchException("Search failed", new RuntimeException())
        );

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber);

        assertFalse(exists);
    }

    @Test
    void should_return_true_when_multiple_cases_found() {
        String appealReferenceNumber = "PA/12345/2023";
        CcdSearchResult result = new CcdSearchResult(3, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber);

        assertTrue(exists);
    }
}

