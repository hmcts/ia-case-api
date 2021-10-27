package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;


class AsylumFieldCaseNameFixerTest {

    private AsylumFieldCaseNameFixer asylumFieldCaseNameFixer;
    private AsylumCase asylumCase;

    @BeforeEach
    public void setUp() {

        asylumFieldCaseNameFixer = new AsylumFieldCaseNameFixer(CASE_NAME, APPEAL_REFERENCE_NUMBER, APPELLANT_FAMILY_NAME);

        asylumCase = new AsylumCase();
    }

    @Test
    void transposes_asylum_case_name_for_old_cases() {

        final String expectedCaseName = "HU/50004/2021-Smith";
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "HU/50004/2021");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Smith");

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(CASE_NAME, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void transposes_asylum_case_name_for_draft_cases() {

        final String expectedCaseName = "HU/50004/2021-Smith";
        asylumCase.write(CASE_NAME, "DRAFT-Smith");
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "HU/50004/2021");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Smith");

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(CASE_NAME, String.class).get())
            .isEqualTo(expectedCaseName);
    }
}
