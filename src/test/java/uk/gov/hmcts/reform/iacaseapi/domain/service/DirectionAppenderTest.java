package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DirectionAppenderTest {

    @Mock private DateProvider dateProvider;
    @Mock private IdValue<Direction> existingDirectionById1;
    @Mock private IdValue<Direction> existingDirectionById2;
    private String newDirectionExplanation = "New direction";
    private Parties newDirectionParties = Parties.BOTH;
    private String newDirectionDateDue = "2018-12-25";
    private String expectedDateSent = LocalDate.MAX.toString();
    private DirectionTag expectedDirectionTag = DirectionTag.RESPONDENT_REVIEW;

    private DirectionAppender directionAppender;

    @Before
    public void setUp() {
        directionAppender = new DirectionAppender(dateProvider);
    }

    @Test
    public void should_append_new_direction_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        Direction existingDirection1 = mock(Direction.class);
        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);

        Direction existingDirection2 = mock(Direction.class);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(existingDirectionById1, existingDirectionById2);

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedDirectionTag
            );

        verify(existingDirectionById1, never()).getId();
        verify(existingDirectionById2, never()).getId();

        assertNotNull(allDirections);
        assertEquals(3, allDirections.size());

        assertEquals("3", allDirections.get(0).getId());
        assertEquals(newDirectionExplanation, allDirections.get(0).getValue().getExplanation());
        assertEquals(newDirectionParties, allDirections.get(0).getValue().getParties());
        assertEquals(newDirectionDateDue, allDirections.get(0).getValue().getDateDue());
        assertEquals(expectedDateSent, allDirections.get(0).getValue().getDateSent());
        assertEquals(expectedDateSent, allDirections.get(0).getValue().getDateSent());
        assertEquals(expectedDirectionTag, allDirections.get(0).getValue().getDirectionTag());

        assertEquals("2", allDirections.get(1).getId());
        assertEquals(existingDirection1, allDirections.get(1).getValue());

        assertEquals("1", allDirections.get(2).getId());
        assertEquals(existingDirection2, allDirections.get(2).getValue());
    }

    @Test
    public void should_return_new_documents_if_no_existing_documents_present() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Direction>> existingDirections =
            Collections.emptyList();

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedDirectionTag
            );

        assertNotNull(allDirections);
        assertEquals(1, allDirections.size());

        assertEquals("1", allDirections.get(0).getId());
        assertEquals(newDirectionExplanation, allDirections.get(0).getValue().getExplanation());
        assertEquals(newDirectionParties, allDirections.get(0).getValue().getParties());
        assertEquals(newDirectionDateDue, allDirections.get(0).getValue().getDateDue());
        assertEquals(expectedDateSent, allDirections.get(0).getValue().getDateSent());
        assertEquals(expectedDirectionTag, allDirections.get(0).getValue().getDirectionTag());
    }

    @Test
    public void should_not_allow_null_arguments() {

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(existingDirectionById1);

        assertThatThrownBy(() ->
            directionAppender.append(
                null,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedDirectionTag
            ))
            .hasMessage("existingDirections must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                null,
                newDirectionParties,
                newDirectionDateDue,
                expectedDirectionTag
            ))
            .hasMessage("explanation must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                null,
                newDirectionDateDue,
                expectedDirectionTag
            ))
            .hasMessage("parties must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                null,
                expectedDirectionTag
            ))
            .hasMessage("dateDue must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                null
            ))
            .hasMessage("directionTag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
