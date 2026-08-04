package uk.gov.hmcts.reform.iacaseapi.infrastructure.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryInfo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataCaseAccessApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.SearchResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CcdSupplementaryDetailsSearchServiceTest {

    @Mock
    private CcdDataCaseAccessApi coreCaseDataApi;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator s2sAuthTokenGenerator;
    @Mock
    private CaseDetails caseDetails;
    @Mock
    SearchResult searchResult;
    @Captor
    ArgumentCaptor<String> queryCaptor;

    private CcdSupplementaryDetailsSearchService ccdSupplementaryDetailsSearchService;
    private static final int MAX_RECORDS = 100;
    private static final int THREAD_POOL_SIZE = 10;
    private final String authorisation = "Bearer token";
    private final String serviceToken = "Bearer serviceToken";

    private final String caseType = "Asylum";
    private final long caseId = 1234;
    private final List<String> ccdCaseNumberList = Arrays.asList("11111111111111", "22222222222222", "99999999999999");

    private Map<String, Object> data = new HashMap<>();
    private ExecutorService executorService;
    CcdElasticSearchQueryBuilder queryBuilder = new CcdElasticSearchQueryBuilder();

    @BeforeEach
    void setUp() {
        data = new HashMap<>();
        data.put("appellantFamilyName", "Johnson");
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        when(idamService.getServiceUserToken()).thenReturn("Bearer token");
        when(s2sAuthTokenGenerator.generate()).thenReturn(serviceToken);

        when(coreCaseDataApi.searchCases(eq(authorisation), eq(serviceToken), eq(caseType), anyString()))
            .thenReturn(searchResult);

        ccdSupplementaryDetailsSearchService = new CcdSupplementaryDetailsSearchService(
            idamService,
            coreCaseDataApi,
            s2sAuthTokenGenerator,
            executorService,
            MAX_RECORDS,
            new CcdElasticSearchQueryBuilder()
        );
    }

    @Test
    void should_return_supplementary_details() {
        when(searchResult.getCases()).thenReturn(List.of(caseDetails));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(data);

        List<SupplementaryInfo> supplementaryInfoList =
            ccdSupplementaryDetailsSearchService.getSupplementaryDetails(ccdCaseNumberList);

        assertEquals(1, supplementaryInfoList.size());
        assertEquals("1234", supplementaryInfoList.getFirst().getCcdCaseNumber());
        assertEquals("Johnson", supplementaryInfoList.getFirst().getSupplementaryDetails().getSurname());

        verify(idamService).getServiceUserToken();
        verify(coreCaseDataApi).searchCases(eq(authorisation), eq(serviceToken), eq(caseType), queryCaptor.capture());
        String capturedQuery = queryCaptor.getValue();
        JSONObject queryJson = new JSONObject(capturedQuery);
        assertEquals(MAX_RECORDS, queryJson.get("size"));
        assertEquals(0, queryJson.get("from"));
        Map<String, Object> queryMap = queryJson.getJSONObject("query").toMap();
        JSONArray sortList = queryJson.getJSONArray("sort");
        Map<String, Object> sortMap = ((JSONObject) sortList.get(0)).toMap();
        assertTrue(sortMap.containsKey("created_date"));
        Map<String, Object> createdDateMap = (Map<String, Object>) sortMap.get("created_date");
        assertTrue(createdDateMap.containsKey("order"));
        assertEquals("desc", createdDateMap.get("order"));
        assertTrue(queryMap.containsKey("terms"));
        Map<String, Object> terms = (Map<String, Object>) queryMap.get("terms");
        assertTrue(terms.containsKey("reference"));
        List<String> capturedCcdCaseNumbers = (List<String>) terms.get("reference");
        assertEquals(ccdCaseNumberList, capturedCcdCaseNumbers);
        assertTrue(terms.containsKey("boost"));
        assertEquals(1, terms.get("boost"));
    }

    @Test
    void should_handle_error() {

        List<SupplementaryInfo> supplementaryInfoList =
            ccdSupplementaryDetailsSearchService.getSupplementaryDetails(ccdCaseNumberList);

        assertEquals(0, supplementaryInfoList.size());
        verify(idamService).getServiceUserToken();
        verify(coreCaseDataApi).searchCases(eq(authorisation), eq(serviceToken), eq(caseType), queryCaptor.capture());
        String capturedQuery = queryCaptor.getValue();
        JSONObject queryJson = new JSONObject(capturedQuery);
        assertEquals(MAX_RECORDS, queryJson.get("size"));
        assertEquals(0, queryJson.get("from"));
        Map<String, Object> queryMap = queryJson.getJSONObject("query").toMap();
        JSONArray sortList = queryJson.getJSONArray("sort");
        Map<String, Object> sortMap = ((JSONObject) sortList.get(0)).toMap();
        assertTrue(sortMap.containsKey("created_date"));
        Map<String, Object> createdDateMap = (Map<String, Object>) sortMap.get("created_date");
        assertTrue(createdDateMap.containsKey("order"));
        assertEquals("desc", createdDateMap.get("order"));
        assertTrue(queryMap.containsKey("terms"));
        Map<String, Object> terms = (Map<String, Object>) queryMap.get("terms");
        assertTrue(terms.containsKey("reference"));
        List<String> capturedCcdCaseNumbers = (List<String>) terms.get("reference");
        assertEquals(ccdCaseNumberList, capturedCcdCaseNumbers);
        assertTrue(terms.containsKey("boost"));
        assertEquals(1, terms.get("boost"));
    }
}
