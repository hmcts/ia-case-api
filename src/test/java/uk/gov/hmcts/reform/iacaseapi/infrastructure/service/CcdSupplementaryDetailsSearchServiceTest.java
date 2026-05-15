package uk.gov.hmcts.reform.iacaseapi.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryInfo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataCaseAccessApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.SearchResult;

@ExtendWith(MockitoExtension.class)
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
    private String expectedQuery;

    @BeforeEach
    void setUp() {
        data = new HashMap<>();
        data.put("appellantFamilyName", "Johnson");

        expectedQuery = buildExpectedQuery(ccdCaseNumberList, MAX_RECORDS);

        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        when(idamService.getServiceUserToken()).thenReturn("Bearer token");
        when(s2sAuthTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi.searchCases(authorisation, serviceToken, caseType, expectedQuery))
                .thenReturn(searchResult);

        ccdSupplementaryDetailsSearchService = new CcdSupplementaryDetailsSearchService(
                idamService,
                coreCaseDataApi,
                s2sAuthTokenGenerator,
                executorService,
                MAX_RECORDS
        );
    }

    @Test
    void should_return_supplementary_details() {
        when(searchResult.getCases()).thenReturn(Arrays.asList(caseDetails));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(data);

        List<SupplementaryInfo> supplementaryInfoList =
                ccdSupplementaryDetailsSearchService.getSupplementaryDetails(ccdCaseNumberList);

        assertEquals(1, supplementaryInfoList.size());
        assertEquals("1234", supplementaryInfoList.getFirst().getCcdCaseNumber());
        assertEquals("Johnson", supplementaryInfoList.getFirst().getSupplementaryDetails().getSurname());

        verify(idamService).getServiceUserToken();
        verify(coreCaseDataApi).searchCases(authorisation, serviceToken, caseType, expectedQuery);
    }

    @Test
    void should_handle_error() {
        List<SupplementaryInfo> supplementaryInfoList =
                ccdSupplementaryDetailsSearchService.getSupplementaryDetails(ccdCaseNumberList);

        assertEquals(0, supplementaryInfoList.size());
        verify(idamService).getServiceUserToken();
        verify(coreCaseDataApi).searchCases(authorisation, serviceToken, caseType, expectedQuery);
    }

    // Mirrors buildSearchQuery() in the service exactly
    private String buildExpectedQuery(List<String> caseReferences, int maxRecords) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            root.put("size", maxRecords);
            root.put("from", 0);

            ArrayNode sortArray = mapper.createArrayNode();
            ObjectNode sortField = mapper.createObjectNode();
            ObjectNode sortOrder = mapper.createObjectNode();
            sortOrder.put("order", "desc");
            sortField.set("created_date", sortOrder);
            sortArray.add(sortField);
            root.set("sort", sortArray);

            ArrayNode valuesArray = mapper.createArrayNode();
            caseReferences.forEach(valuesArray::add);

            ObjectNode termsNode = mapper.createObjectNode();
            termsNode.set("reference", valuesArray);
            termsNode.put("boost", 1.0);

            ObjectNode queryNode = mapper.createObjectNode();
            queryNode.set("terms", termsNode);

            root.set("query", queryNode);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build expected ES query", e);
        }
    }
}