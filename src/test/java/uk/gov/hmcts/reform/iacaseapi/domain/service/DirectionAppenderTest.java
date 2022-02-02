package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DirectionAppenderTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private WaFieldsPublisher waFieldsPublisher;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private IdValue<Direction> existingDirectionById1;
    @Mock
    private IdValue<Direction> existingDirectionById2;
    private String newDirectionExplanation = "New direction";
    private Parties newDirectionParties = Parties.BOTH;
    private String newDirectionDateDue = "2018-12-25";
    private String expectedDateSent = LocalDate.MAX.toString();
    private DirectionTag expectedTag = DirectionTag.RESPONDENT_REVIEW;

    private DirectionAppender directionAppender;

    @BeforeEach
    public void setUp() {
        directionAppender = new DirectionAppender(dateProvider, waFieldsPublisher);
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
                asylumCase,
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
        verify(waFieldsPublisher).addLastModifiedDirection(eq(asylumCase), anyString(), any(Parties.class), anyString(), any(DirectionTag.class), anyString(), eq(null));
    }

    @Test
    void should_return_new_documents_if_no_existing_documents_present() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Direction>> existingDirections =
            emptyList();

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                asylumCase,
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
        verify(waFieldsPublisher).addLastModifiedDirection(eq(asylumCase), anyString(), any(Parties.class), anyString(), any(DirectionTag.class), anyString(), eq(null));
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<Direction>> existingDirections =
            asList(existingDirectionById1);

        assertThatThrownBy(() ->
            directionAppender.append(
                asylumCase,
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
                asylumCase,
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
                asylumCase,
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
                asylumCase,
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
                asylumCase,
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
    void should_append_direction_with_dirction_type() {
        String directionType = "someEventDirectionType";
        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        Direction existingDirection1 = mock(Direction.class);
        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);

        Direction existingDirection2 = mock(Direction.class);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        List<IdValue<Direction>> existingDirections =
                asList(existingDirectionById1, existingDirectionById2);

        List<IdValue<ClarifyingQuestion>> newQuestions =
                asList(new IdValue<>("1", new ClarifyingQuestion("Question 1")));
        List<IdValue<Direction>> allDirections =
                directionAppender.append(
                        asylumCase,
                        existingDirections,
                        newDirectionExplanation,
                        newDirectionParties,
                        newDirectionDateDue,
                        expectedTag,
                        directionType
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
        assertEquals(emptyList(), allDirections.get(0).getValue().getClarifyingQuestions());
        assertNotNull(allDirections.get(0).getValue().getUniqueId());
        assertThat(UUID.fromString(allDirections.get(0).getValue().getUniqueId())).isExactlyInstanceOf(UUID.class);
        assertEquals(directionType, allDirections.get(0).getValue().getDirectionType());

        assertEquals("2", allDirections.get(1).getId());
        assertEquals(existingDirection1, allDirections.get(1).getValue());

        assertEquals("1", allDirections.get(2).getId());
        assertEquals(existingDirection2, allDirections.get(2).getValue());
        verify(waFieldsPublisher).addLastModifiedDirection(eq(asylumCase), anyString(), any(Parties.class), anyString(), any(DirectionTag.class), anyString(), eq(directionType));
    }

    @Test
    void should_addpend_direction_with_questions() {
        String directionType = "someEventDirectionType";
        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        Direction existingDirection1 = mock(Direction.class);
        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);

        Direction existingDirection2 = mock(Direction.class);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        List<IdValue<Direction>> existingDirections =
            asList(existingDirectionById1, existingDirectionById2);

        List<IdValue<ClarifyingQuestion>> newQuestions =
            asList(new IdValue<>("1", new ClarifyingQuestion("Question 1")));
        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                asylumCase,
                existingDirections,
                newDirectionExplanation,
                newDirectionParties,
                newDirectionDateDue,
                expectedTag,
                newQuestions,
                directionType
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
        assertNotNull(allDirections.get(0).getValue().getUniqueId());
        assertThat(UUID.fromString(allDirections.get(0).getValue().getUniqueId())).isExactlyInstanceOf(UUID.class);
        assertEquals(directionType, allDirections.get(0).getValue().getDirectionType());

        assertEquals("2", allDirections.get(1).getId());
        assertEquals(existingDirection1, allDirections.get(1).getValue());

        assertEquals("1", allDirections.get(2).getId());
        assertEquals(existingDirection2, allDirections.get(2).getValue());
        verify(waFieldsPublisher).addLastModifiedDirection(eq(asylumCase), anyString(), any(Parties.class), anyString(), any(DirectionTag.class), anyString(), eq(directionType));
    }
}
