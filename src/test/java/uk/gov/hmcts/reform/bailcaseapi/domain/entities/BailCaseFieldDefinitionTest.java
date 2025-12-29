package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.MakeNewApplicationService;

public class BailCaseFieldDefinitionTest {
    /**
     * Check if the fields need to be added/removed on the VALID_FIELDS list in.
     * @see MakeNewApplicationService
     */
    @Test
    void fail_if_changes_needed_after_modifying_bail_case_definition() {
        assertEquals(310, BailCaseFieldDefinition.values().length);
    }

    @Test
    void should_find_enum_if_value_match() throws JsonProcessingException {
        Stream.of(
            "applicantGivenNames",
            "applicantGender",
            "bailReferenceNumber"
        ).forEach(valueToSearch -> {
            BailCaseFieldDefinition caseFieldDefinition = BailCaseFieldDefinition.getEnumFromString(valueToSearch);

            assertEquals(valueToSearch, caseFieldDefinition.value());

            if (valueToSearch.equals("applicantGivenNames")) {
                assertEquals(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, caseFieldDefinition);
            } else if (valueToSearch.equals("applicantGender")) {
                assertEquals(BailCaseFieldDefinition.APPLICANT_GENDER, caseFieldDefinition);
            } else {
                assertEquals(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, caseFieldDefinition);
            }
        });
    }

    @Test
    void should_throw_exception_if_enum_not_found_from_value() throws JsonProcessingException {

        String valueToFetch = "DUMMY_FAIL_VALUE";

        assertThatThrownBy(() -> BailCaseFieldDefinition.getEnumFromString(valueToFetch))
            .hasMessage("No BailCaseFieldDefinition found with the value: " + valueToFetch)
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
