package uk.gov.hmcts.reform.iacaseapi.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;


class AssignmentTest extends SpringBootIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserialize_as_expected_for_unknown_values() throws IOException {
        String jsonContent = "{\"id\":\"id\","
                + "\"authorisations\":[],"
                + "\"actorIdType\":\"actorIdType\","
                + "\"actorId\":\"actorId\","
                + "\"roleType\":\"roleType\","
                + "\"roleName\":\"tribunal-caseworker\","
                + "\"roleCategory\":\"roleCategory\","
                + "\"classification\":\"classification\","
                + "\"grantType\":\"grantType\","
                + "\"readOnly\":true,"
                + "\"attributes\":{}"
                + "}";

        final Assignment expected = objectMapper.readValue(jsonContent, Assignment.class);

        Assignment actual = getAssignmentForUnknownValues();

        assertEquals(expected, actual);
    }

    private Assignment getAssignmentForUnknownValues() {
        Assignment assignment = new Assignment(
                "id",
                null,
                Collections.emptyList(),
                ActorIdType.UNKNOWN,
                "actorId",
                RoleType.UNKNOWN,
                RoleName.TRIBUNAL_CASEWORKER,
                RoleCategory.UNKNOWN,
                Classification.UNKNOWN,
                GrantType.UNKNOWN,
                true,
                Collections.emptyMap()
        );
        return assignment;
    }
}
