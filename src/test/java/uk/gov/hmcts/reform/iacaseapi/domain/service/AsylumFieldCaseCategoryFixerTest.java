package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

class AsylumFieldCaseCategoryFixerTest {

    private AsylumFieldCaseCategoryFixer asylumFieldCaseCategoryFixer;
    private AsylumCase asylumCase;

    @BeforeEach
    public void setUp() {

        asylumFieldCaseCategoryFixer = new AsylumFieldCaseCategoryFixer(HMCTS_CASE_CATEGORY, APPEAL_TYPE);

        asylumCase = new AsylumCase();
    }

    @Test
    void transposes_rp_asylum_hmcts_case_category() {

        final String expectedCaseCategory = "Revocation";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.RP));

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }

    @Test
    void transposes_pa_asylum_hmcts_case_category() {

        final String expectedCaseCategory = "Protection";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }

    @Test
    void transposes_ea_asylum_hmcts_case_category() {

        final String expectedCaseCategory = "EEA";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }

    @Test
    void transposes_hu_asylum_hmcts_case_category() {

        final String expectedCaseCategory = "Human rights";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }

    @Test
    void transposes_dc_asylum_hmcts_case_category() {

        final String expectedCaseCategory = "DoC";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.DC));

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }

    @Test
    void transposes_EU_asylum_hmcts_case_category() {

        final String expectedCaseCategory = "EU Settlement Scheme";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EU));

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }

    @Test
    void transposes_and_format_asylum_hmcts_case_category_if_already_exists_and_is_incorrect() {

        final String expectedCaseCategory = "Human rights";
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));
        asylumCase.write(HMCTS_CASE_CATEGORY, "Incorrect-CaseCategory");

        asylumFieldCaseCategoryFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_CATEGORY, String.class).get())
            .isEqualTo(expectedCaseCategory);
    }
}
