package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

class AsylumFieldNameFixerTest {

    AsylumFieldNameFixer asylumFieldNameFixer;
    AsylumCase asylumCase;

    @BeforeEach
    void setUp() {

        asylumFieldNameFixer = new AsylumFieldNameFixer(
            APPEAL_REFERENCE_NUMBER,
            HOME_OFFICE_REFERENCE_NUMBER);

        asylumCase = new AsylumCase();
    }

    @Test
    void transposes_asylum_case_values() {

        asylumCase.write(APPEAL_REFERENCE_NUMBER, "valueTobeTransitioned");

        asylumFieldNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .isEmpty();

        assertThat(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("valueTobeTransitioned");
    }

    @Test
    void does_nothing_if_from_field_not_present() {

        asylumFieldNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .isEmpty();

        assertThat(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .isEmpty();
    }

    @Test
    void maintain_correct_field_name_when_data_already_in_correct_state() {

        asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, "valueAssignedToCorrectField");

        asylumFieldNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .isEmpty();

        assertThat(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("valueAssignedToCorrectField");
    }
}