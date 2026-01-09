package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchRepository;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSearchQuery;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdCase;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdSearchResult;

@Disabled
@ExtendWith(MockitoExtension.class)
class AppealReferenceNumberSearchServiceTest {

    private static final String CCD_REF_NUMBER = "1234567890123456";

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
    void should_return_true_when_single_result_is_current_case() {
        String appealReferenceNumber = "PA/12345/2023";
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("ccdReferenceNumberForDisplay", CCD_REF_NUMBER);
        CcdCase ccdCase = new CcdCase(123456L, 123456L, caseData);
        CcdSearchResult result = new CcdSearchResult(1, List.of(ccdCase));

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber, CCD_REF_NUMBER);

        assertTrue(exists);
        verify(queryBuilder).buildAppealReferenceNumberQuery(appealReferenceNumber);
        verify(searchRepository).searchCases(query);
    }

    @Test
    void should_return_false_when_single_result_is_different_case() {
        String appealReferenceNumber = "PA/12345/2023";
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("ccdReferenceNumberForDisplay", "9999999999999999");
        CcdCase ccdCase = new CcdCase(123456L, 123456L, caseData);
        CcdSearchResult result = new CcdSearchResult(1, List.of(ccdCase));

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber, CCD_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_appeal_reference_number_does_not_exist() {
        String appealReferenceNumber = "PA/12345/2023";
        CcdSearchResult result = new CcdSearchResult(0, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber, CCD_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_appeal_reference_number_is_null() {
        boolean exists = service.appealReferenceNumberExists(null, CCD_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_appeal_reference_number_is_empty() {
        boolean exists = service.appealReferenceNumberExists("", CCD_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_search_result_is_null() {
        String appealReferenceNumber = "PA/12345/2023";

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(null);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber, CCD_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_search_throws_exception() {
        String appealReferenceNumber = "PA/12345/2023";

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(any())).thenThrow(
            new CcdElasticSearchRepository.CcdSearchException("Search failed", new RuntimeException())
        );

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber, CCD_REF_NUMBER);

        assertFalse(exists);
    }

    @Test
    void should_return_false_when_multiple_cases_found() {
        String appealReferenceNumber = "PA/12345/2023";
        CcdSearchResult result = new CcdSearchResult(3, Collections.emptyList());

        when(queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber)).thenReturn(query);
        when(searchRepository.searchCases(query)).thenReturn(result);

        boolean exists = service.appealReferenceNumberExists(appealReferenceNumber, CCD_REF_NUMBER);

        assertFalse(exists);
    }
}

