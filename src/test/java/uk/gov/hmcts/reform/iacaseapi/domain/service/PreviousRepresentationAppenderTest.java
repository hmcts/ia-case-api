package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousRepresentation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PreviousRepresentationAppenderTest {

    @Mock
    private IdValue<PreviousRepresentation> existingPreviousRepresentationById1;
    @Mock
    private IdValue<PreviousRepresentation> existingPreviousRepresentationById2;
    @Mock
    private PreviousRepresentation existingPreviousRepresentation1 = mock(PreviousRepresentation.class);
    @Mock
    private PreviousRepresentation existingPreviousRepresentation2 = mock(PreviousRepresentation.class);
    @Mock
    private PreviousRepresentation newPreviousRepresentation1 = mock(PreviousRepresentation.class);

    private PreviousRepresentationAppender previousRepresentationAppender = new PreviousRepresentationAppender();

    @Test
    void should_append_previous_representation_in_first_position() {

        List<IdValue<PreviousRepresentation>> existingPreviousRepresentations =
            Arrays.asList(
                existingPreviousRepresentationById1,
                existingPreviousRepresentationById2
            );

        PreviousRepresentation newPreviousRepresentation = newPreviousRepresentation1;

        when(existingPreviousRepresentationById1.getValue()).thenReturn(existingPreviousRepresentation1);
        when(existingPreviousRepresentationById2.getValue()).thenReturn(existingPreviousRepresentation2);

        final List<IdValue<PreviousRepresentation>> allPreviousRepresentations =
            previousRepresentationAppender.append(existingPreviousRepresentations, newPreviousRepresentation);

        verify(existingPreviousRepresentationById1, never()).getId();
        verify(existingPreviousRepresentationById2, never()).getId();

        assertNotNull(allPreviousRepresentations);
        assertEquals(3, allPreviousRepresentations.size());

        assertEquals("3", allPreviousRepresentations.get(0).getId());
        assertEquals(newPreviousRepresentation1, allPreviousRepresentations.get(0).getValue());

        assertEquals("2", allPreviousRepresentations.get(1).getId());
        assertEquals(existingPreviousRepresentation1, allPreviousRepresentations.get(1).getValue());

        assertEquals("1", allPreviousRepresentations.get(2).getId());
        assertEquals(existingPreviousRepresentation2, allPreviousRepresentations.get(2).getValue());
    }

    @Test
    void should_return_new_previous_representation_if_no_existing_previous_representations_present() {

        List<IdValue<PreviousRepresentation>> existingPreviousRepresentations = Collections.emptyList();

        final List<IdValue<PreviousRepresentation>> allPreviousRepresentations =
            previousRepresentationAppender.append(existingPreviousRepresentations, newPreviousRepresentation1);

        assertNotNull(allPreviousRepresentations);
        assertEquals(1, allPreviousRepresentations.size());

        assertEquals("1", allPreviousRepresentations.get(0).getId());
        assertEquals(newPreviousRepresentation1, allPreviousRepresentations.get(0).getValue());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<PreviousRepresentation>> existingPreviousRepresentations = Arrays.asList(existingPreviousRepresentationById1);
        PreviousRepresentation newPreviousRepresentation = newPreviousRepresentation1;

        assertThatThrownBy(() -> previousRepresentationAppender.append(null, newPreviousRepresentation))
            .hasMessage("existingPreviousRepresentations must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> previousRepresentationAppender.append(existingPreviousRepresentations, null))
            .hasMessage("newPreviousRepresentation must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
