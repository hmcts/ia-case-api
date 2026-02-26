package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builder for creating Elasticsearch queries for CCD case searches.
 * Based on Elasticsearch Query DSL for searching case data.
 */
@Component
public class CcdElasticSearchQueryBuilder {

    private static final int DEFAULT_SIZE = 10;

    /**
     * Builds an Elasticsearch query to search for cases by appeal reference number.
     * Uses match_phrase query for exact phrase matching on analyzed text fields.
     * match_phrase ensures the entire phrase matches in order, preventing partial matches.
     *
     * @param appealReferenceNumber The appeal reference number to search for
     * @return CcdSearchQuery object containing the Elasticsearch query
     */
    public CcdSearchQuery buildAppealReferenceNumberQuery(String appealReferenceNumber) {
        return buildAppealReferenceNumberQuery(appealReferenceNumber, null);
    }

    /**
     * Builds an Elasticsearch query to search for cases by appeal reference number,
     * optionally excluding a specific case by its CCD reference number.
     *
     * <p>This is useful when editing an existing appeal to avoid false positive duplicates.
     * When a user edits an appeal without changing the reference number, we need to exclude
     * the current case from the duplicate check to avoid incorrectly flagging it as a duplicate.
     *
     * <p>The exclusion is implemented using an Elasticsearch must_not clause with a term query
     * on the case reference field. The CCD reference number is normalized by removing spaces
     * before comparison (e.g., "1234 5678 9012 3456" becomes "1234567890123456").
     *
     * @param appealReferenceNumber The appeal reference number to search for
     * @param ccdRefNumber The CCD reference number of the current case to exclude (can be null)
     * @return CcdSearchQuery object containing the Elasticsearch query
     */
    public CcdSearchQuery buildAppealReferenceNumberQuery(String appealReferenceNumber, String ccdRefNumber) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        List<Map<String, Object>> must = List.of(
            createMatchPhraseQuery("data.appealReferenceNumber", appealReferenceNumber)
        );
        bool.put("must", must);

        // Add must_not clause to exclude current case when editing
        if (ccdRefNumber != null && !ccdRefNumber.isEmpty()) {
            String ccdRefWithoutSpaces = ccdRefNumber.replaceAll("\\s", "");
            try {
                Long caseReference = Long.parseLong(ccdRefWithoutSpaces);
                Map<String, Object> mustNot = new HashMap<>();
                mustNot.put("term", Map.of("reference", caseReference));
                bool.put("must_not", List.of(mustNot));
            } catch (NumberFormatException e) {
                // If the CCD reference is not a valid number, skip the exclusion.
                // This is a defensive check; in normal operation the CCD reference should always be numeric.
            }
        }

        query.put("bool", bool);

        return new CcdSearchQuery(
            query,
            DEFAULT_SIZE,
            List.of("reference", "data.appealReferenceNumber", "id")
        );
    }

    /**
     * Creates a match_phrase query for exact phrase matching on analyzed text fields.
     * match_phrase matches the exact phrase in order, preventing partial token matches.
     * This ensures "HU/12345/2025" only matches "HU/12345/2025" and not "HU/12344/2022".
     *
     * @param field The field to match
     * @param value The phrase value to match exactly
     * @return Map representing a match_phrase query
     */
    private Map<String, Object> createMatchPhraseQuery(String field, String value) {
        Map<String, Object> matchPhrase = new HashMap<>();
        Map<String, Object> fieldValue = new HashMap<>();
        fieldValue.put(field, value);
        matchPhrase.put("match_phrase", fieldValue);
        return matchPhrase;
    }
}

