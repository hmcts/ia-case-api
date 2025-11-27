package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdElasticSearchRepository;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSearchQuery;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdCase;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdSearchResult;

/**
 * Service for searching cases by appeal reference number using Elasticsearch.
 * This service provides a high-level API for checking if an appeal reference number exists.
 */
@Slf4j
@Service
public class AppealReferenceNumberSearchService {

    private final CcdElasticSearchQueryBuilder queryBuilder;
    private final CcdElasticSearchRepository searchRepository;

    public AppealReferenceNumberSearchService(
        CcdElasticSearchQueryBuilder queryBuilder,
        CcdElasticSearchRepository searchRepository
    ) {
        this.queryBuilder = queryBuilder;
        this.searchRepository = searchRepository;
    }

    /**
     * Checks if an appeal reference number already exists in CCD.
     *
     * @param appealReferenceNumber The appeal reference number to check
     * @return true if the reference number exists, false otherwise
     */
    public boolean appealReferenceNumberExists(String appealReferenceNumber) {
        if (appealReferenceNumber == null || appealReferenceNumber.isEmpty()) {
            log.warn("Attempted to search for null or empty appeal reference number");
            return false;
        }

        try {
            log.info("Searching for existing cases with appeal reference number: {}", appealReferenceNumber);
            
            CcdSearchQuery query = queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber);
            CcdSearchResult result = searchRepository.searchCases(query);

            log.info("CCD search result for appeal reference number {}: total={}, cases={}", 
                appealReferenceNumber, 
                result != null ? result.getTotal() : "null",
                result != null && result.getCases() != null ? result.getCases().size() : "null");

            if (result == null) {
                log.warn("Received null result from CCD search");
                return false;
            }

            if (result.getCases() != null && !result.getCases().isEmpty()) {
                log.info("Case details found for appeal reference number {}:", appealReferenceNumber);
                for (int i = 0; i < result.getCases().size(); i++) {
                    CcdCase ccdCase = result.getCases().get(i);
                    log.info("  Case[{}]: id={}, reference={}, appealReferenceNumber={}", 
                        i + 1,
                        ccdCase.getId(),
                        ccdCase.getReference(),
                        ccdCase.getData() != null ? ccdCase.getData().get("appealReferenceNumber") : "null");
                    log.info("  Case[{}] data: {}", i + 1, ccdCase.getData());
                }
            }

            boolean exists = result.getTotal() > 0;
            
            if (exists) {
                log.info("Found {} existing case(s) with appeal reference number: {}", 
                    result.getTotal(), appealReferenceNumber);
            } else {
                log.info("No existing cases found with appeal reference number: {}", appealReferenceNumber);
            }

            return exists;

        } catch (CcdElasticSearchRepository.CcdSearchException e) {
            log.error("Error searching for appeal reference number: {}", appealReferenceNumber, e);
            // In case of search failure, we'll return false to avoid blocking the operation
            return false;
        }
    }
}

