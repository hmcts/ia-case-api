package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
public class CaseFlagAppenderTest {

    @Mock private CaseFlag caseFlag1;
    @Mock private CaseFlag caseFlag2;
    @Mock private IdValue<CaseFlag> existingCaseFlagById1;
    @Mock private IdValue<CaseFlag> existingCaseFlagById2;

    private final CaseFlagType newCaseFlagType = CaseFlagType.ANONYMITY;
    private final String newCaseFlagAdditionalInformation = "some additional information";

    private CaseFlagAppender caseFlagAppender;

    @Before
    public void setUp() {
        caseFlagAppender = new CaseFlagAppender();
        when(caseFlag1.getCaseFlagType()).thenReturn(CaseFlagType.COMPLEX_CASE);
        when(caseFlag2.getCaseFlagType()).thenReturn(CaseFlagType.UNACCOMPANIED_MINOR);
    }

    @Test
    public void should_append_new_case_flag_in_first_position() {

        CaseFlag existingCaseFlag1 = caseFlag1;
        when(existingCaseFlagById1.getValue()).thenReturn(existingCaseFlag1);

        CaseFlag existingCaseFlag2 = caseFlag2;
        when(existingCaseFlagById2.getValue()).thenReturn(existingCaseFlag2);

        List<IdValue<CaseFlag>> existingCaseFlags = Arrays.asList(existingCaseFlagById1, existingCaseFlagById2);

        List<IdValue<CaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            newCaseFlagType,
            newCaseFlagAdditionalInformation
        );

        verify(existingCaseFlagById1, never()).getId();
        verify(existingCaseFlagById2, never()).getId();

        assertNotNull(allCaseFlags);
        assertEquals(3, allCaseFlags.size());

        assertEquals("1", allCaseFlags.get(2).getId());

        assertEquals(newCaseFlagType, allCaseFlags.get(2).getValue().getCaseFlagType());
        assertEquals(newCaseFlagAdditionalInformation, allCaseFlags.get(2).getValue().getCaseFlagAdditionalInformation());

        assertEquals("2", allCaseFlags.get(1).getId());
        assertEquals(existingCaseFlag2, allCaseFlags.get(1).getValue());

        assertEquals("3", allCaseFlags.get(0).getId());
        assertEquals(existingCaseFlag1, allCaseFlags.get(0).getValue());
    }

    @Test
    public void should_return_new_case_flags_if_no_existing_case_flags_present() {
        List<IdValue<CaseFlag>> existingCaseFlags = Collections.emptyList();

        List<IdValue<CaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            newCaseFlagType,
            newCaseFlagAdditionalInformation
        );

        assertNotNull(allCaseFlags);
        assertEquals(1, allCaseFlags.size());

        assertEquals("1", allCaseFlags.get(0).getId());
        assertEquals(newCaseFlagType, allCaseFlags.get(0).getValue().getCaseFlagType());
        assertEquals(newCaseFlagAdditionalInformation, allCaseFlags.get(0).getValue().getCaseFlagAdditionalInformation());
    }

    @Test
    public void should_not_allow_null_arguments() {
        List<IdValue<CaseFlag>> existingCaseFlags = Collections.singletonList(existingCaseFlagById1);

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
