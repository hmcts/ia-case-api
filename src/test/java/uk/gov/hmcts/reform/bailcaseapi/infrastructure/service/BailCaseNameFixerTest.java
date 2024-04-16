package uk.gov.hmcts.reform.bailcaseapi.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailCaseNameFixerTest {

    private BailFieldCaseNameFixer bailFieldCaseNameFixer;
    private BailCase bailCase;

    @Mock
    private BailCase bailCaseMock;

    @BeforeEach
    public void setUp() {
        bailFieldCaseNameFixer = new BailFieldCaseNameFixer(CASE_NAME_HMCTS_INTERNAL, APPLICANT_GIVEN_NAMES,
                                                            APPLICANT_FAMILY_NAME
        );
        bailCase = new BailCase();
    }

    @Test
    void transposes_bail_case_name() {
        final String expectedCaseName = "John Smith";
        bailCase.write(APPLICANT_GIVEN_NAMES, "John");
        bailCase.write(APPLICANT_FAMILY_NAME, "Smith");

        bailFieldCaseNameFixer.fix(bailCase);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void transposes_and_format_bail_case_name() {
        final String expectedCaseName = "John Smith";
        bailCase.write(APPLICANT_GIVEN_NAMES, "John ");
        bailCase.write(APPLICANT_FAMILY_NAME, "  Smith");

        bailFieldCaseNameFixer.fix(bailCase);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void transposes_and_format_bail_case_name_if_already_exists_and_is_incorrect() {
        final String expectedCaseName = "John Smith";
        bailCase.write(APPLICANT_GIVEN_NAMES, "John ");
        bailCase.write(APPLICANT_FAMILY_NAME, "  Smith");
        bailCase.write(CASE_NAME_HMCTS_INTERNAL, "Incorrect-CaseName");

        bailFieldCaseNameFixer.fix(bailCase);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void does_not_set_case_name_when_applicatant_given_names_is_not_present() {
        final String expectedCaseName = "Some-CaseName";
        bailCase.write(APPLICANT_FAMILY_NAME, "Smith");
        bailCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);

        bailFieldCaseNameFixer.fix(bailCase);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void does_not_set_case_name_when_applicatant_family_name_is_not_present() {
        final String expectedCaseName = "Some-CaseName";
        bailCase.write(APPLICANT_GIVEN_NAMES, "John ");
        bailCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);

        bailFieldCaseNameFixer.fix(bailCase);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void should_set_case_name_hmcts_internal_if_not_present() {
        setupAndTestForSuccessfulFix();

        verify(bailCaseMock, times(1)).write(
            BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL, "John Smith");
    }

    private void setupAndTestForSuccessfulFix() {
        when(bailCaseMock.read(CASE_NAME_HMCTS_INTERNAL)).thenReturn(Optional.empty());
        when(bailCaseMock.read(APPLICANT_GIVEN_NAMES)).thenReturn(Optional.of("John "));
        when(bailCaseMock.read(APPLICANT_FAMILY_NAME)).thenReturn(Optional.of("Smith"));

        bailFieldCaseNameFixer.fix(bailCaseMock);
    }

}
