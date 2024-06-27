package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumFieldCaseNameFixerTest {

    private AsylumFieldCaseNameFixer asylumFieldCaseNameFixer;
    private AsylumCase asylumCase;

    @Mock
    private AsylumCase asylumCaseMock;

    @Mock
    private FeatureToggler featureToggler;

    @BeforeEach
    public void setUp() {
        when(featureToggler.getValue("wa-R3-feature", false)).thenReturn(true);

        asylumFieldCaseNameFixer = new AsylumFieldCaseNameFixer(HMCTS_CASE_NAME_INTERNAL, APPELLANT_GIVEN_NAMES, APPELLANT_FAMILY_NAME, featureToggler);

        asylumCase = new AsylumCase();
    }

    @Test
    void transposes_asylum_case_name() {

        final String expectedCaseName = "John Smith";
        asylumCase.write(APPELLANT_GIVEN_NAMES, "John");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Smith");

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
        assertThat(asylumCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void transposes_and_format_asylum_case_name() {

        final String expectedCaseName = "John Smith";
        asylumCase.write(APPELLANT_GIVEN_NAMES, "John ");
        asylumCase.write(APPELLANT_FAMILY_NAME, "  Smith");

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
        assertThat(asylumCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void transposes_and_format_asylum_case_name_if_already_exists_and_is_incorrect() {

        final String expectedCaseName = "John Smith";
        asylumCase.write(APPELLANT_GIVEN_NAMES, "John ");
        asylumCase.write(APPELLANT_FAMILY_NAME, "  Smith");
        asylumCase.write(HMCTS_CASE_NAME_INTERNAL, "Incorrect-CaseName");
        asylumCase.write(CASE_NAME_HMCTS_INTERNAL, "Incorrect-CaseName");

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
        assertThat(asylumCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void does_not_set_case_name_when_appellant_given_names_is_not_present() {

        final String expectedCaseName = "Some-CaseName";
        asylumCase.write(APPELLANT_FAMILY_NAME, "Smith");
        asylumCase.write(HMCTS_CASE_NAME_INTERNAL, expectedCaseName);
        asylumCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
        assertThat(asylumCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void does_not_set_case_name_when_appellant_family_name_is_not_present() {

        final String expectedCaseName = "Some-CaseName";
        asylumCase.write(APPELLANT_GIVEN_NAMES, "John ");
        asylumCase.write(HMCTS_CASE_NAME_INTERNAL, expectedCaseName);
        asylumCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);

        asylumFieldCaseNameFixer.fix(asylumCase);

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
        assertThat(asylumCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo(expectedCaseName);
    }

    @Test
    void should_set_case_name_hmcts_internal_if_not_present() {
        setupAndTestForSuccessfulFix();

        verify(asylumCaseMock, times(1)).write(
            AsylumCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL, "John Smith");
    }

    private void setupAndTestForSuccessfulFix() {
        when(asylumCaseMock.read(HMCTS_CASE_NAME_INTERNAL)).thenReturn(Optional.of("John Smith"));
        when(asylumCaseMock.read(APPELLANT_GIVEN_NAMES)).thenReturn(Optional.of("John "));
        when(asylumCaseMock.read(APPELLANT_FAMILY_NAME)).thenReturn(Optional.of("Smith"));

        asylumFieldCaseNameFixer.fix(asylumCaseMock);
    }

    @Test
    void should_do_nothing_as_feature_disabled() {
        when(featureToggler.getValue("wa-R3-feature", false)).thenReturn(false);

        setupAndTestForSuccessfulFix();

        verify(asylumCaseMock, never()).write(
            eq(AsylumCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL), anyString());
    }
}
