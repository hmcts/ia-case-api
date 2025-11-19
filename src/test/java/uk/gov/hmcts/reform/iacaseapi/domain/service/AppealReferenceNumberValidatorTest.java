package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppealReferenceNumberValidatorTest {

    @Mock
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    private AppealReferenceNumberValidator appealReferenceNumberValidator;

    @BeforeEach
    void setUp() {
        appealReferenceNumberValidator = new AppealReferenceNumberValidator(appealReferenceNumberGenerator);
    }

    @Test
    void should_return_empty_list_when_reference_number_is_valid_and_does_not_exist() {
        String validReferenceNumber = "PA/12345/2025";

        when(appealReferenceNumberGenerator.referenceNumberExists(validReferenceNumber))
            .thenReturn(false);

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
        verify(appealReferenceNumberGenerator).referenceNumberExists(validReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_exists() {
        String existingReferenceNumber = "HU/67890/2024";

        when(appealReferenceNumberGenerator.referenceNumberExists(existingReferenceNumber))
            .thenReturn(true);

        List<String> errors = appealReferenceNumberValidator.validate(existingReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number already exists. Please enter a different reference number.");
        verify(appealReferenceNumberGenerator).referenceNumberExists(existingReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_is_null() {
        List<String> errors = appealReferenceNumberValidator.validate(null);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Appeal reference number cannot be null or empty");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(null);
    }

    @Test
    void should_return_error_when_reference_number_is_empty() {
        List<String> errors = appealReferenceNumberValidator.validate("");

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Appeal reference number cannot be null or empty");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists("");
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_wrong_appeal_type() {
        String invalidReferenceNumber = "XX/12345/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_few_digits_in_sequence() {
        String invalidReferenceNumber = "PA/1234/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_many_digits_in_sequence() {
        String invalidReferenceNumber = "PA/123456/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_wrong_year_format() {
        String invalidReferenceNumber = "PA/12345/1999";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_missing_slashes() {
        String invalidReferenceNumber = "PA123452025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_few_parts() {
        String invalidReferenceNumber = "PA/12345";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_number_has_invalid_format_too_many_parts() {
        String invalidReferenceNumber = "PA/12345/2025/extra";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors).contains("The reference number is in an incorrect format. Please enter valid format of XX/00000/0000");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_validate_all_valid_appeal_types() {
        String[] validAppealTypes = {"HU", "DA", "DC", "EA", "PA", "RP", "LE", "LD", "LP", "LH", "LR", "IA"};

        for (String appealType : validAppealTypes) {
            String validReferenceNumber = appealType + "/12345/2025";

            when(appealReferenceNumberGenerator.referenceNumberExists(validReferenceNumber))
                .thenReturn(false);

            List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

            assertThat(errors).isEmpty();
        }
    }

    @Test
    void should_not_check_existence_when_format_is_invalid() {
        String invalidReferenceNumber = "INVALID/12345/2025";

        List<String> errors = appealReferenceNumberValidator.validate(invalidReferenceNumber);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("The reference number is in an incorrect format");
        verify(appealReferenceNumberGenerator, never()).referenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_validate_edge_case_year_2000() {
        String validReferenceNumber = "PA/12345/2000";

        when(appealReferenceNumberGenerator.referenceNumberExists(validReferenceNumber))
            .thenReturn(false);

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_edge_case_year_2099() {
        String validReferenceNumber = "PA/12345/2099";

        when(appealReferenceNumberGenerator.referenceNumberExists(validReferenceNumber))
            .thenReturn(false);

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_sequence_with_leading_zeros() {
        String validReferenceNumber = "HU/00001/2024";

        when(appealReferenceNumberGenerator.referenceNumberExists(validReferenceNumber))
            .thenReturn(false);

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_sequence_with_max_digits() {
        String validReferenceNumber = "EA/99999/2023";

        when(appealReferenceNumberGenerator.referenceNumberExists(validReferenceNumber))
            .thenReturn(false);

        List<String> errors = appealReferenceNumberValidator.validate(validReferenceNumber);

        assertThat(errors).isEmpty();
    }
}

