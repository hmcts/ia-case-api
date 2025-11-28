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
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = List.of(
            createMatchPhraseQuery("data.appealReferenceNumber", appealReferenceNumber)
        );

        bool.put("must", must);
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

