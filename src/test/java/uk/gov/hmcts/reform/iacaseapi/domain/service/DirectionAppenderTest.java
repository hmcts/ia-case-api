package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
public class DirectionAppenderTest {

    @Mock private AsylumCase asylumCase;
    @Mock private IdValue<Direction> existingDirectionById1;
    @Mock private IdValue<Direction> existingDirectionById2;
    @Mock private Direction newDirection;

    @Captor private ArgumentCaptor<List<IdValue<Direction>>> newDirectionsCaptor;

    private DirectionAppender directionAppender =
        new DirectionAppender();

    @Test
    public void should_append_new_direction_in_first_position() {

        Direction existingDirection1 = mock(Direction.class);
        Direction existingDirection2 = mock(Direction.class);

        when(existingDirectionById1.getValue()).thenReturn(existingDirection1);
        when(existingDirectionById2.getValue()).thenReturn(existingDirection2);

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(existingDirectionById1, existingDirectionById2);

        when(asylumCase.getDirections()).thenReturn(Optional.of(existingDirections));

        directionAppender.append(asylumCase, newDirection);

        verify(existingDirectionById1, never()).getId();
        verify(existingDirectionById2, never()).getId();

        verify(asylumCase, times(1)).getDirections();
        verify(asylumCase, times(1)).setDirections(newDirectionsCaptor.capture());

        List<IdValue<Direction>> actualNewDirections = newDirectionsCaptor.getAllValues().get(0);

        assertNotNull(actualNewDirections);
        assertEquals(3, actualNewDirections.size());

        assertEquals("3", actualNewDirections.get(0).getId());
        assertEquals(newDirection, actualNewDirections.get(0).getValue());

        assertEquals("2", actualNewDirections.get(1).getId());
        assertEquals(existingDirection1, actualNewDirections.get(1).getValue());

        assertEquals("1", actualNewDirections.get(2).getId());
        assertEquals(existingDirection2, actualNewDirections.get(2).getValue());
    }

    @Test
    public void should_return_new_direction_if_no_existing_directions_present() {

        when(asylumCase.getDirections()).thenReturn(Optional.empty());

        directionAppender.append(asylumCase, newDirection);

        verify(asylumCase, times(1)).getDirections();
        verify(asylumCase, times(1)).setDirections(newDirectionsCaptor.capture());

        List<IdValue<Direction>> actualNewDirections = newDirectionsCaptor.getAllValues().get(0);

        assertNotNull(actualNewDirections);
        assertEquals(1, actualNewDirections.size());

        assertEquals("1", actualNewDirections.get(0).getId());
        assertEquals(newDirection, actualNewDirections.get(0).getValue());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> directionAppender.append(null, newDirection))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> directionAppender.append(asylumCase, null))
            .hasMessage("newDirection must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
