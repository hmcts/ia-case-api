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

    private static final String CCD_REF_NUMBER = "1234567890123456";
    private static final String APPEAL_REF_NUMBER = "PA/12345/2023";

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
    void should_return_true_when_appeal_reference_exists() {
        CcdSearchResult result = new CcdSearchResult(1, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, null)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER);

        assertTrue(exists);
        verify(queryBuilder).buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, null);
        verify(searchRepository).searchCases(query);
    }

    @Test
    void should_return_true_when_appeal_reference_exists_excluding_current_case() {
        // The query builder will handle excluding the current case via must_not clause
        CcdSearchResult result = new CcdSearchResult(1, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, CCD_REF_NUMBER)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER, CCD_REF_NUMBER);

        assertTrue(exists);
        verify(queryBuilder).buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, CCD_REF_NUMBER);
        verify(searchRepository).searchCases(query);
    }

    @Test
    void should_return_false_when_appeal_reference_number_does_not_exist() {
        CcdSearchResult result = new CcdSearchResult(0, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, null)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_only_result_is_current_case() {
        // When editing, if the only match is the current case, the query builder excludes it via must_not
        // So the result will have total=0
        CcdSearchResult result = new CcdSearchResult(0, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, CCD_REF_NUMBER)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER, CCD_REF_NUMBER);

        assertFalse(exists);
        verify(queryBuilder).buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, CCD_REF_NUMBER);
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
        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, null)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(null);

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_search_throws_exception() {
        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, null)).thenReturn(query);
        when(searchRepository.searchCases(any())).thenThrow(
            new CcdElasticSearchRepository.CcdSearchException("Search failed", new RuntimeException())
        );

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_true_when_multiple_cases_found() {
        // Multiple cases means there's definitely a duplicate
        CcdSearchResult result = new CcdSearchResult(3, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(APPEAL_REF_NUMBER, null)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(APPEAL_REF_NUMBER);

        assertTrue(exists);
    }
}

