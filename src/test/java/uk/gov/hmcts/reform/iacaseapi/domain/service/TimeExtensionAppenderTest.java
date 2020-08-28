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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TimeExtensionAppenderTest {

    @Mock private DateProvider dateProvider;
    @Mock private IdValue<TimeExtension> existingTimeExtensionById1;
    @Mock private IdValue<TimeExtension> existingTimeExtensionById2;
    String newTimeExtensionReason = "New direction";
    State caseState = State.AWAITING_REASONS_FOR_APPEAL;
    List<IdValue<Document>> newTimeExtensionEvidence = Collections.emptyList();
    String expectedDateRequested = LocalDate.MAX.toString();

    TimeExtensionAppender timeExtensionAppender;

    @BeforeEach
    void setUp() {

        timeExtensionAppender = new TimeExtensionAppender(dateProvider);
    }

    @Test
    void should_append_new_time_extension_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        TimeExtension existingTimeExtension1 = mock(TimeExtension.class);
        when(existingTimeExtensionById1.getValue()).thenReturn(existingTimeExtension1);

        TimeExtension existingTimeExtension2 = mock(TimeExtension.class);
        when(existingTimeExtensionById2.getValue()).thenReturn(existingTimeExtension2);

        List<IdValue<TimeExtension>> existingTimeExtensions =
            asList(existingTimeExtensionById1, existingTimeExtensionById2);

        List<IdValue<TimeExtension>> allTimeExtensions =
            timeExtensionAppender.append(
                existingTimeExtensions,
                caseState,
                newTimeExtensionReason,
                newTimeExtensionEvidence
            );

        verify(existingTimeExtensionById1, never()).getId();
        verify(existingTimeExtensionById2, never()).getId();

        assertNotNull(allTimeExtensions);
        assertEquals(3, allTimeExtensions.size());

        assertEquals("3", allTimeExtensions.get(0).getId());
        assertEquals(newTimeExtensionReason, allTimeExtensions.get(0).getValue().getReason());
        assertEquals(newTimeExtensionEvidence, allTimeExtensions.get(0).getValue().getEvidence());
        assertEquals(expectedDateRequested, allTimeExtensions.get(0).getValue().getRequestDate());
        assertEquals(caseState, allTimeExtensions.get(0).getValue().getState());
        assertEquals(TimeExtensionStatus.SUBMITTED, allTimeExtensions.get(0).getValue().getStatus());

        assertEquals("2", allTimeExtensions.get(1).getId());
        assertEquals(existingTimeExtension1, allTimeExtensions.get(1).getValue());

        assertEquals("1", allTimeExtensions.get(2).getId());
        assertEquals(existingTimeExtension2, allTimeExtensions.get(2).getValue());
    }

    @Test
    void should_return_new_documents_if_no_existing_documents_present() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<TimeExtension>> existingTimeExtensions = Collections.emptyList();


        List<IdValue<TimeExtension>> allTimeExtensions =
            timeExtensionAppender.append(
                existingTimeExtensions,
                caseState,
                newTimeExtensionReason,
                newTimeExtensionEvidence
            );

        assertNotNull(allTimeExtensions);
        assertEquals(1, allTimeExtensions.size());

        assertEquals("1", allTimeExtensions.get(0).getId());
        assertEquals(newTimeExtensionReason, allTimeExtensions.get(0).getValue().getReason());
        assertEquals(newTimeExtensionEvidence, allTimeExtensions.get(0).getValue().getEvidence());
        assertEquals(expectedDateRequested, allTimeExtensions.get(0).getValue().getRequestDate());
        assertEquals(caseState, allTimeExtensions.get(0).getValue().getState());
        assertEquals(TimeExtensionStatus.SUBMITTED, allTimeExtensions.get(0).getValue().getStatus());
    }

    @Test
    void should_not_allow_null_arguments() {

        List<IdValue<TimeExtension>> existingTimeExtensions =
            asList(existingTimeExtensionById1);

        assertThatThrownBy(() ->
            timeExtensionAppender.append(
                null,
                caseState,
                newTimeExtensionReason,
                newTimeExtensionEvidence
            ))
            .hasMessage("existingTimeExtension must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            timeExtensionAppender.append(
                existingTimeExtensions,
                null,
                newTimeExtensionReason,
                newTimeExtensionEvidence
            ))
            .hasMessage("state must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() ->
            timeExtensionAppender.append(
                existingTimeExtensions,
                caseState,
                null,
                newTimeExtensionEvidence
            ))
            .hasMessage("reason must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
