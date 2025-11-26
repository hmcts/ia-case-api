package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CcdElasticSearchQueryBuilderTest {

    private CcdElasticSearchQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        queryBuilder = new CcdElasticSearchQueryBuilder();
    }

    @Test
    void should_build_appeal_reference_number_query() {
        String appealReferenceNumber = "PA/12345/2023";

        CcdSearchQuery query = queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber);

        assertNotNull(query);
        assertNotNull(query.getQuery());
        assertEquals(10, query.getSize());
        assertEquals(List.of("reference", "data.appealReferenceNumber", "id"), query.getSource());
    }

    @Test
    void should_include_bool_query_structure() {
        String appealReferenceNumber = "PA/12345/2023";

        CcdSearchQuery query = queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber);

        Map<String, Object> queryMap = query.getQuery();
        assertThat(queryMap).containsKey("bool");

        @SuppressWarnings("unchecked")
        Map<String, Object> bool = (Map<String, Object>) queryMap.get("bool");
        assertThat(bool).containsKey("must");
    }

    @Test
    void should_include_three_must_clauses() {
        String appealReferenceNumber = "PA/12345/2023";

        CcdSearchQuery query = queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber);

        @SuppressWarnings("unchecked")
        Map<String, Object> bool = (Map<String, Object>) query.getQuery().get("bool");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> must = (List<Map<String, Object>>) bool.get("must");
        
        assertEquals(3, must.size());
    }

    @Test
    void should_include_correct_source_fields() {
        String appealReferenceNumber = "PA/12345/2023";

        CcdSearchQuery query = queryBuilder.buildAppealReferenceNumberQuery(appealReferenceNumber);

        assertThat(query.getSource())
            .contains("reference", "data.appealReferenceNumber", "id");
    }
}

