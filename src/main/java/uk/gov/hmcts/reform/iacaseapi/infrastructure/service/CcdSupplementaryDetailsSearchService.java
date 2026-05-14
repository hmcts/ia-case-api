package uk.gov.hmcts.reform.iacaseapi.infrastructure.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryInfo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.SupplementaryDetailsService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataCaseAccessApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.SearchResult;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdSupplementaryDetailsSearchService implements SupplementaryDetailsService {
    private static final String CASE_TYPE_ID = "Asylum";
    private static final String APPELLANT_FAMILY_NAME = "appellantFamilyName";
    private static final String APPEAL_REFERENCE_NUMBER = "appealReferenceNumber";
    private final CcdDataCaseAccessApi ccdDataCaseAccessApi;
    private final IdamService idamService;
    private final AuthTokenGenerator s2sAuthTokenGenerator;
    private final int maxRecords;
    private final ExecutorService executorService;

    @Autowired
    public CcdSupplementaryDetailsSearchService(IdamService idamService,
                                                CcdDataCaseAccessApi ccdDataCaseAccessApi,
                                                AuthTokenGenerator s2sAuthTokenGenerator,
                                                @Qualifier("fixedThreadPool") ExecutorService executorService,
                                                @Value("${requestPagination.maxRecords}") int maxRecords
    ) {
        this.idamService = idamService;
        this.ccdDataCaseAccessApi = ccdDataCaseAccessApi;
        this.s2sAuthTokenGenerator = s2sAuthTokenGenerator;
        this.executorService = executorService;
        this.maxRecords = maxRecords;
    }

    @Override
    public List<SupplementaryInfo> getSupplementaryDetails(List<String> ccdCaseNumberList) {

        log.info(
            "CcdSupplementaryDetailsSearchService : getSupplementaryDetails fetch data for the case ids  {} ",
            ccdCaseNumberList.toString()
        );

        String userToken;
        String s2sToken;

        try {
            userToken = idamService.getServiceUserToken();
            log.info(
                "CcdSupplementaryDetailsSearchService : getSupplementaryDetails System user token has been generated."
            );

            // returned token is already with Bearer prefix
            s2sToken = s2sAuthTokenGenerator.generate();
            log.info("S2S token has been generated.");

        } catch (Exception e) {
            throw new IdentityManagerResponseException(e.getMessage(), e);
        }

        List<CompletableFuture<List<SupplementaryInfo>>> completableFutureList = new ArrayList<>();
        final Collection<List<String>> chunkedCcdCaseList = getChunkedCcdCaseList(ccdCaseNumberList);

        for (List<String> splitCcdCaseList : chunkedCcdCaseList) {
            CompletableFuture<List<SupplementaryInfo>> completableFuture = CompletableFuture.supplyAsync(
                    () -> {
                        String query = buildSearchQuery(splitCcdCaseList);
                        return search(userToken, s2sToken, query);
                    },
                    executorService
            );
            completableFutureList.add(completableFuture);
        }

        List<SupplementaryInfo> allResults = new ArrayList<>();

        for (CompletableFuture<List<SupplementaryInfo>> completableFutureResult : completableFutureList) {
            try {
                allResults.addAll(completableFutureResult.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                log.error(e.getMessage(), e);
            }
        }

        return allResults;
    }

    private String buildSearchQuery(List<String> caseReferences) {
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

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build ES query", e);
        }
    }

    private List<SupplementaryInfo> search(String userAuthorisation, String serviceAuthToken, String query) {

        SearchResult searchResult = ccdDataCaseAccessApi.searchCases(
            userAuthorisation,
            serviceAuthToken,
            CASE_TYPE_ID,
            query
        );

        if (searchResult.getCases() == null) {
            return Collections.emptyList();
        }
        return searchResult.getCases()
            .stream()
            .filter(p -> p.getCaseData() != null && p.getId() != null)
            .map(this::extractSupplementaryInfo)
            .toList();
    }

    private SupplementaryInfo extractSupplementaryInfo(CaseDetails caseDetails) {
        SupplementaryDetails supplementaryDetails = new SupplementaryDetails(
            String.valueOf(caseDetails.getCaseData().get(APPELLANT_FAMILY_NAME)),
            String.valueOf(caseDetails.getCaseData().get(APPEAL_REFERENCE_NUMBER))
        );

        log.info("Supplementary details for caseId {} - surname: {}, caseReferenceNumber: {}",
                 caseDetails.getId(), supplementaryDetails.getSurname(), supplementaryDetails.getCaseReferenceNumber());
        return new SupplementaryInfo(
            String.valueOf(caseDetails.getId()),
            supplementaryDetails
        );
    }

    private Collection<List<String>> getChunkedCcdCaseList(List<String> ccdCaseNumberList) {

        final AtomicInteger counter = new AtomicInteger();

        return ccdCaseNumberList.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / maxRecords))
            .values();
    }
}
