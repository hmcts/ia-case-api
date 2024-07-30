package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
public class AsylumFieldLegalRepNameFixerTest {

    private static final String LEGAL_REP_LEGACY_NAME_1 = "firstName lastName lastName";
    private static final String LEGAL_REP_FIRST_NAME = "firstName";
    private static final String LEGAL_REP_LAST_NAME = "lastName lastName";
    private static final String LEGAL_REP_LEGACY_NAME_2 = "firstNameLastName";

    @Mock
    private AsylumCase asylumCase;

    private AsylumFieldLegalRepNameFixer legalRepNameFixer;

    @BeforeEach
    void setup() {
        legalRepNameFixer = new AsylumFieldLegalRepNameFixer();
    }

    @Test
    void should_not_split_name_if_last_name_present() {

        legalRepNameFixer.fix(asylumCase);

        verify(asylumCase, never()).write(eq(LEGAL_REP_NAME), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_FAMILY_NAME), anyString());
    }

    @Test
    void should_split_name_if_last_name_not_present() {
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(LEGAL_REP_LEGACY_NAME_1));
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());

        legalRepNameFixer.fix(asylumCase);

        verify(asylumCase, times(1)).read(LEGAL_REP_NAME, String.class);
        verify(asylumCase, times(1)).write(LEGAL_REP_NAME, LEGAL_REP_FIRST_NAME);
        verify(asylumCase, times(1)).write(LEGAL_REP_FAMILY_NAME, LEGAL_REP_LAST_NAME);
    }

    @Test
    void should_not_throw_outOfBounds_exception_if_first_name_has_no_space() {
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(LEGAL_REP_LEGACY_NAME_2));
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());

        legalRepNameFixer.fix(asylumCase);

        verify(asylumCase, times(1)).read(LEGAL_REP_NAME, String.class);
        verify(asylumCase, times(1)).write(LEGAL_REP_NAME, LEGAL_REP_LEGACY_NAME_2);
        verify(asylumCase, times(1)).write(LEGAL_REP_FAMILY_NAME, " ");
    }

}
