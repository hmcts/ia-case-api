package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DirectionAppenderTest {

    @Mock private DateProvider dateProvider;
    @Mock private IdValue<Direction> existingDirectionById1;
    @Mock private IdValue<Direction> existingDirectionById2;
    String newDirectionExplanation = "New direction";
    Parties newDirectionParties = Parties.BOTH;
    String newDirectionDateDue = "2018-12-25";
    String expectedDateSent = LocalDate.MAX.toString();
    DirectionTag expectedTag = DirectionTag.RESPONDENT_REVIEW;

    DirectionAppender directionAppender;

    @BeforeEach
    void setUp() {

        directionAppender = new DirectionAppender(dateProvider);
    }

    @Test
    void should_append_new_direction_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        Direction existingDirection1 = mock(Direction.class);
        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);

        Direction existingDirection2 = mock(Direction.class);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        List<IdValue<Direction>> existingDirections =
            asList(existingDirectionById1, existingDirectionById2);

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedTag
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
        assertEquals(expectedTag, allDirections.get(0).getValue().getTag());

        assertEquals("2", allDirections.get(1).getId());
        assertEquals(existingDirection1, allDirections.get(1).getValue());

        assertEquals("1", allDirections.get(2).getId());
        assertEquals(existingDirection2, allDirections.get(2).getValue());
    }

    @Test
    void should_return_new_documents_if_no_existing_documents_present() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Direction>> existingDirections =
            Collections.emptyList();

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedTag
            );

        assertNotNull(allDirections);
        assertEquals(1, allDirections.size());

        assertEquals("1", allDirections.get(0).getId());
        assertEquals(newDirectionExplanation, allDirections.get(0).getValue().getExplanation());
        assertEquals(newDirectionParties, allDirections.get(0).getValue().getParties());
        assertEquals(newDirectionDateDue, allDirections.get(0).getValue().getDateDue());
        assertEquals(expectedDateSent, allDirections.get(0).getValue().getDateSent());
        assertEquals(expectedTag, allDirections.get(0).getValue().getTag());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<Direction>> existingDirections =
            asList(existingDirectionById1);

        assertThatThrownBy(() ->
            directionAppender.append(
                null,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedTag
            ))
            .hasMessage("existingDirections must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                null,
                newDirectionParties,
                newDirectionDateDue,
                expectedTag
            ))
            .hasMessage("explanation must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                null,
                newDirectionDateDue,
                expectedTag
            ))
            .hasMessage("parties must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            directionAppender.append(
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                null,
                expectedTag
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
            .hasMessage("tag must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_addpend_direction_with_questions() {
        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        Direction existingDirection1 = mock(Direction.class);
        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);

        Direction existingDirection2 = mock(Direction.class);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        List<IdValue<Direction>> existingDirections =
                asList(existingDirectionById1, existingDirectionById2);

        List<IdValue<ClarifyingQuestion>> newQuestions = asList(new IdValue<>("1", new ClarifyingQuestion("Question 1")));
        List<IdValue<Direction>> allDirections =
                directionAppender.append(
                        existingDirections,
                        newDirectionExplanation,
                        newDirectionParties,
                        newDirectionDateDue,
                        expectedTag,
                        newQuestions
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
        assertEquals(expectedTag, allDirections.get(0).getValue().getTag());
        assertEquals(newQuestions, allDirections.get(0).getValue().getClarifyingQuestions());

        assertEquals("2", allDirections.get(1).getId());
        assertEquals(existingDirection1, allDirections.get(1).getValue());

        assertEquals("1", allDirections.get(2).getId());
        assertEquals(existingDirection2, allDirections.get(2).getValue());
    }
}
