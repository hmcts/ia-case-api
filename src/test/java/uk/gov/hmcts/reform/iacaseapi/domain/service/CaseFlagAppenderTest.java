package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.LegacyCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CaseFlagAppenderTest {

    private final CaseFlagType newCaseFlagType = CaseFlagType.ANONYMITY;
    private final String newCaseFlagAdditionalInformation = "some additional information";
    private LegacyCaseFlag caseFlag1;
    private LegacyCaseFlag caseFlag2;
    @Mock
    private IdValue<LegacyCaseFlag> existingCaseFlagById1;
    @Mock
    private IdValue<LegacyCaseFlag> existingCaseFlagById2;
    private CaseFlagAppender caseFlagAppender;

    @BeforeEach
    public void setUp() {
        caseFlagAppender = new CaseFlagAppender();
<<<<<<< HEAD
        caseFlag1 = new LegacyCaseFlag(CaseFlagType.COMPLEX_CASE, "some info");
=======
        caseFlag1 = new LegacyCaseFlag(CaseFlagType.POTENTIALLY_VIOLENT_PERSON, "some info");
>>>>>>> 819f4cd77ca79ccb00a4f820cb604a183d0204f2
        caseFlag2 = new LegacyCaseFlag(CaseFlagType.UNACCOMPANIED_MINOR, "some info");
    }

    @Test
    void should_append_new_case_flag_in_first_position() {

        when(existingCaseFlagById1.getValue()).thenReturn(caseFlag1);
        when(existingCaseFlagById2.getValue()).thenReturn(caseFlag2);

        List<IdValue<LegacyCaseFlag>> existingCaseFlags = Arrays.asList(existingCaseFlagById1, existingCaseFlagById2);

        List<IdValue<LegacyCaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            newCaseFlagType,
            newCaseFlagAdditionalInformation
        );

        verify(existingCaseFlagById1, never()).getId();
        verify(existingCaseFlagById2, never()).getId();

        assertNotNull(allCaseFlags);
        assertEquals(3, allCaseFlags.size());

        assertEquals("1", allCaseFlags.get(2).getId());

        assertEquals(newCaseFlagType, allCaseFlags.get(2).getValue().getLegacyCaseFlagType());
        assertEquals(newCaseFlagAdditionalInformation,
            allCaseFlags.get(2).getValue().getLegacyCaseFlagAdditionalInformation());

        assertEquals("2", allCaseFlags.get(1).getId());
        assertEquals(caseFlag2, allCaseFlags.get(1).getValue());

        assertEquals("3", allCaseFlags.get(0).getId());
        assertEquals(caseFlag1, allCaseFlags.get(0).getValue());
    }

    @Test
    void should_return_new_case_flags_if_no_existing_case_flags_present() {
        List<IdValue<LegacyCaseFlag>> existingCaseFlags = Collections.emptyList();

        List<IdValue<LegacyCaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            newCaseFlagType,
            newCaseFlagAdditionalInformation
        );

        assertNotNull(allCaseFlags);
        assertEquals(1, allCaseFlags.size());

        assertEquals("1", allCaseFlags.get(0).getId());
        assertEquals(newCaseFlagType, allCaseFlags.get(0).getValue().getLegacyCaseFlagType());
        assertEquals(newCaseFlagAdditionalInformation,
            allCaseFlags.get(0).getValue().getLegacyCaseFlagAdditionalInformation());
    }

    @Test
    void should_not_allow_null_arguments() {
        List<IdValue<LegacyCaseFlag>> existingCaseFlags = Collections.singletonList(existingCaseFlagById1);

        assertThatThrownBy(() ->
            caseFlagAppender.append(
                null,
                newCaseFlagType,
                newCaseFlagAdditionalInformation
            ))
            .hasMessage("existingCaseFlags must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            caseFlagAppender.append(
                existingCaseFlags,
                null,
                newCaseFlagAdditionalInformation
            ))
            .hasMessage("caseFlagType must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            caseFlagAppender.append(
                existingCaseFlags,
                newCaseFlagType,
                null
            ))
            .hasMessage("caseFlagAdditionalInformation must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
