package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousHearing;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PreviousHearingAppenderTest {

    @Mock private IdValue<PreviousHearing> existingPreviousHearingById1;
    @Mock private IdValue<PreviousHearing> existingPreviousHearingById2;
    @Mock private PreviousHearing existingPreviousHearing1 = mock(PreviousHearing.class);
    @Mock private PreviousHearing existingPreviousHearing2 = mock(PreviousHearing.class);
    @Mock private PreviousHearing newPreviousHearing1 = mock(PreviousHearing.class);

    private PreviousHearingAppender previousHearingAppender = new PreviousHearingAppender();

    @Test
    public void should_append_previous_hearing_in_first_position() {

        List<IdValue<PreviousHearing>> existingPreviousHearings =
            Arrays.asList(
                existingPreviousHearingById1,
                existingPreviousHearingById2
            );

        PreviousHearing newPreviousHearing = newPreviousHearing1;

        when(existingPreviousHearingById1.getValue()).thenReturn(existingPreviousHearing1);
        when(existingPreviousHearingById2.getValue()).thenReturn(existingPreviousHearing2);

        final List<IdValue<PreviousHearing>> allPreviousHearings =
            previousHearingAppender.append(existingPreviousHearings, newPreviousHearing);

        verify(existingPreviousHearingById1, never()).getId();
        verify(existingPreviousHearingById2, never()).getId();

        assertNotNull(allPreviousHearings);
        assertEquals(3, allPreviousHearings.size());

        assertEquals("3", allPreviousHearings.get(0).getId());
        assertEquals(newPreviousHearing1, allPreviousHearings.get(0).getValue());

        assertEquals("2", allPreviousHearings.get(1).getId());
        assertEquals(existingPreviousHearing1, allPreviousHearings.get(1).getValue());

        assertEquals("1", allPreviousHearings.get(2).getId());
        assertEquals(existingPreviousHearing2, allPreviousHearings.get(2).getValue());
    }

    @Test
    public void should_return_new_previous_hearing_if_no_existing_previous_hearings_present() {

        List<IdValue<PreviousHearing>> existingDPreviousHearings = Collections.emptyList();

        final List<IdValue<PreviousHearing>> allPreviousHearings =
            previousHearingAppender.append(existingDPreviousHearings, newPreviousHearing1);

        assertNotNull(allPreviousHearings);
        assertEquals(1, allPreviousHearings.size());

        assertEquals("1", allPreviousHearings.get(0).getId());
        assertEquals(newPreviousHearing1, allPreviousHearings.get(0).getValue());
    }

    @Test
    public void should_not_allow_null_arguments() {

        List<IdValue<PreviousHearing>> existingPreviousHearings = Arrays.asList(existingPreviousHearingById1);
        PreviousHearing newPreviousHearing = newPreviousHearing1;

        assertThatThrownBy(() -> previousHearingAppender.append(null, newPreviousHearing))
            .hasMessage("existingPreviousHearings must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> previousHearingAppender.append(existingPreviousHearings, null))
            .hasMessage("newPreviousHearing must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
