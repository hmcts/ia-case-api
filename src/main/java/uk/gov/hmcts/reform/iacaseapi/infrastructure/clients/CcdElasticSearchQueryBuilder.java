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

    private static final String CASE_TYPE_ID = "Asylum";
    private static final String JURISDICTION = "IA";
    private static final int DEFAULT_SIZE = 10;

    /**
     * Builds an Elasticsearch query to search for cases by appeal reference number.
     * Uses term queries for exact matching of the appeal reference number.
     * Note: Uses .keyword suffix for the appeal reference number field to perform exact match
     * on the non-analyzed keyword subfield.
     *
     * @param appealReferenceNumber The appeal reference number to search for
     * @return CcdSearchQuery object containing the Elasticsearch query
     */
    public CcdSearchQuery buildAppealReferenceNumberQuery(String appealReferenceNumber) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = List.of(
            createTermQuery("data.appealReferenceNumber.keyword", appealReferenceNumber),
            createTermQuery("case_type_id", CASE_TYPE_ID),
            createTermQuery("jurisdiction", JURISDICTION)
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
     * Creates a term query for exact matching of a specific field and value.
     * Term queries perform exact matches without text analysis.
     *
     * @param field The field to match exactly
     * @param value The value to match exactly
     * @return Map representing a term query
     */
    private Map<String, Object> createTermQuery(String field, String value) {
        Map<String, Object> term = new HashMap<>();
        Map<String, Object> fieldValue = new HashMap<>();
        fieldValue.put(field, value);
        term.put("term", fieldValue);
        return term;
    }
}

