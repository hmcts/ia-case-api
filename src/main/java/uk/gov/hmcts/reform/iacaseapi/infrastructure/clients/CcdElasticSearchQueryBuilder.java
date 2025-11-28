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
     * - Uses match query for the appealReferenceNumber field (analyzed text field)
     * - The match query will find exact matches when the entire value matches
     *
     * @param appealReferenceNumber The appeal reference number to search for
     * @return CcdSearchQuery object containing the Elasticsearch query
     */
    public CcdSearchQuery buildAppealReferenceNumberQuery(String appealReferenceNumber) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = List.of(
            createMatchQuery("data.appealReferenceNumber", appealReferenceNumber)
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
     * Creates a match query for text field matching.
     * Match queries work with analyzed text fields.
     *
     * @param field The field to match
     * @param value The value to match
     * @return Map representing a match query
     */
    private Map<String, Object> createMatchQuery(String field, String value) {
        Map<String, Object> match = new HashMap<>();
        Map<String, Object> fieldValue = new HashMap<>();
        fieldValue.put(field, value);
        match.put("match", fieldValue);
        return match;
    }
}

