package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private AppealReferenceNumberSearchService appealReferenceNumberSearchService;

    private AppealReferenceNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AppealReferenceNumberValidator(
            appealReferenceNumberSearchService
        );
    }

    @Test
    void should_pass_validation_for_valid_and_unique_reference_number() {
        String validReferenceNumber = "PA/12345/2023";

        when(appealReferenceNumberSearchService.appealReferenceNumberExists(validReferenceNumber)).thenReturn(false);

        List<String> errors = validator.validate(validReferenceNumber);

        assertTrue(errors.isEmpty());
        verify(appealReferenceNumberSearchService).appealReferenceNumberExists(validReferenceNumber);
    }

    @Test
    void should_return_error_for_null_reference_number() {
        List<String> errors = validator.validate(null);

        assertEquals(1, errors.size());
        assertThat(errors.get(0)).contains("cannot be null or empty");
    }

    @Test
    void should_return_error_for_empty_reference_number() {
        List<String> errors = validator.validate("");

        assertEquals(1, errors.size());
        assertThat(errors.get(0)).contains("cannot be null or empty");
    }

    @Test
    void should_return_error_for_invalid_format() {
        String invalidReferenceNumber = "INVALID/12345/2023";

        List<String> errors = validator.validate(invalidReferenceNumber);

        assertEquals(1, errors.size());
        assertThat(errors.get(0)).contains("incorrect format");
        verify(appealReferenceNumberSearchService, never()).appealReferenceNumberExists(invalidReferenceNumber);
    }

    @Test
    void should_return_error_when_reference_exists_in_ccd() {
        String existingReferenceNumber = "PA/12345/2023";

        when(appealReferenceNumberSearchService.appealReferenceNumberExists(existingReferenceNumber)).thenReturn(true);

        List<String> errors = validator.validate(existingReferenceNumber);

        assertEquals(1, errors.size());
        assertThat(errors.get(0)).contains("already exists");
        verify(appealReferenceNumberSearchService).appealReferenceNumberExists(existingReferenceNumber);
    }

    @Test
    void should_validate_all_valid_appeal_type_prefixes() {
        String[] validPrefixes = {"HU", "DA", "DC", "EA", "PA", "RP", "LE", "LD", "LP", "LH", "LR", "IA"};

        for (String prefix : validPrefixes) {
            String referenceNumber = prefix + "/12345/2023";
            when(appealReferenceNumberSearchService.appealReferenceNumberExists(referenceNumber)).thenReturn(false);

            List<String> errors = validator.validate(referenceNumber);

            assertTrue(errors.isEmpty(), "Expected no errors for prefix: " + prefix);
        }
    }

    @Test
    void should_reject_invalid_year_format() {
        String invalidYearReferenceNumber = "PA/12345/1999";

        List<String> errors = validator.validate(invalidYearReferenceNumber);

        assertEquals(1, errors.size());
        assertThat(errors.get(0)).contains("incorrect format");
    }

    @Test
    void should_reject_invalid_number_length() {
        String invalidNumberLength = "PA/123/2023";

        List<String> errors = validator.validate(invalidNumberLength);

        assertEquals(1, errors.size());
        assertThat(errors.get(0)).contains("incorrect format");
    }

}
