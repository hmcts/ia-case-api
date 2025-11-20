package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppealReferenceNumberValidatorTest {

    private AppealReferenceNumberValidator appealReferenceNumberValidator;

    @BeforeEach
    void setUp() {
        appealReferenceNumberValidator = new AppealReferenceNumberValidator();
    }

    @Test
    void should_return_empty_list_when_reference_number_is_valid() {
        String validReferenceNumber = "PA/12345/2025";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_return_error_when_reference_number_is_null() {
        List<String> errors = appealReferenceNumberValidator.validate(null);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Appeal reference number cannot be null or empty");
    }

    @Test
    void should_return_error_when_reference_number_is_empty() {
        List<String> errors = appealReferenceNumberValidator.validate("");

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Appeal reference number cannot be null or empty");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_wrong_appeal_type() {
        String invalidReferenceNumber = "XX/12345/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_few_digits_in_sequence() {
        String invalidReferenceNumber = "PA/1234/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_many_digits_in_sequence() {
        String invalidReferenceNumber = "PA/123456/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_wrong_year_format() {
        String invalidReferenceNumber = "PA/12345/1999";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_missing_slashes() {
        String invalidReferenceNumber = "PA123452025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_few_parts() {
        String invalidReferenceNumber = "PA/12345";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_many_parts() {
        String invalidReferenceNumber = "PA/12345/2025/extra";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_validate_all_valid_appeal_types() {
        String[] validAppealTypes = {"HU", "DA", "DC", "EA", "PA", "RP", "LE", "LD", "LP", "LH", "LR", "IA"};

        for (String appealType : validAppealTypes) {
            String validReferenceNumber = appealType + "/12345/2025";

            List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

            assertThat(errors).isEmpty();
        }
    }

    @Test
    void should_return_error_when_format_is_invalid() {
        String invalidReferenceNumber = "INVALID/12345/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_validate_edge_case_year_2000() {
        String validReferenceNumber = "PA/12345/2000";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_edge_case_year_2099() {
        String validReferenceNumber = "PA/12345/2099";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_sequence_with_leading_zeros() {
        String validReferenceNumber = "HU/00001/2024";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_sequence_starting_with_0() {
        String validReferenceNumber = "EA/01234/2023";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_sequence_starting_with_1() {
        String validReferenceNumber = "EA/12345/2023";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_sequence_starting_with_2() {
        String validReferenceNumber = "EA/23456/2023";

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_return_error_when_sequence_starts_with_3() {
        String invalidReferenceNumber = "EA/34567/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_sequence_starts_with_4() {
        String invalidReferenceNumber = "EA/45678/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_sequence_starts_with_5() {
        String invalidReferenceNumber = "EA/56789/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_sequence_starts_with_6() {
        String invalidReferenceNumber = "EA/67890/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_sequence_starts_with_7() {
        String invalidReferenceNumber = "EA/78901/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_sequence_starts_with_8() {
        String invalidReferenceNumber = "EA/89012/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }

    @Test
    void should_return_error_when_sequence_starts_with_9() {
        String invalidReferenceNumber = "EA/99999/2023";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format.");
    }
}

