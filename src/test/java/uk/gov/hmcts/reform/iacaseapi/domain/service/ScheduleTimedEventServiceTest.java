package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleTimedEventServiceTest {

    @Mock
    private DateProvider dateProvider;
    
    @Mock
    private Scheduler scheduler;
    
    @Captor
    private ArgumentCaptor<TimedEvent> timedEventCaptor;

    private ScheduleTimedEventService scheduleTimedEventService;

    @BeforeEach
    void setUp() {
        scheduleTimedEventService = new ScheduleTimedEventService(dateProvider, scheduler);
    }

    @Test
    void should_schedule_timed_event_with_provided_date() {
        String caseId = "1234567890";
        Event event = Event.RE_TRIGGER_WA_TASKS;
        ZonedDateTime scheduledDate = ZonedDateTime.now().plusHours(1);

        scheduleTimedEventService.scheduleTimedEvent(caseId, scheduledDate, event);

        verify(scheduler).schedule(timedEventCaptor.capture());
        TimedEvent capturedEvent = timedEventCaptor.getValue();

        assertEquals(Long.parseLong(caseId), capturedEvent.getCaseId());
        assertEquals(event, capturedEvent.getEvent());
        assertEquals(scheduledDate, capturedEvent.getScheduledDateTime());
        assertEquals("IA", capturedEvent.getJurisdiction());
        assertEquals("Asylum", capturedEvent.getCaseType());
    }

    @Test
    void should_schedule_timed_event_now() {
        String caseId = "9876543210";
        Event event = Event.RE_TRIGGER_WA_TASKS;
        LocalDateTime now = LocalDateTime.now();
        
        when(dateProvider.nowWithTime()).thenReturn(now);

        scheduleTimedEventService.scheduleTimedEventNow(caseId, event);

        verify(scheduler).schedule(timedEventCaptor.capture());
        TimedEvent capturedEvent = timedEventCaptor.getValue();

        ZonedDateTime expectedDate = ZonedDateTime.of(now, ZoneId.systemDefault());
        assertEquals(Long.parseLong(caseId), capturedEvent.getCaseId());
        assertEquals(event, capturedEvent.getEvent());
        assertEquals(expectedDate, capturedEvent.getScheduledDateTime());
    }

    @Test
    void should_handle_scheduler_exception_gracefully() {
        String caseId = "1111111111";
        Event event = Event.RE_TRIGGER_WA_TASKS;
        ZonedDateTime scheduledDate = ZonedDateTime.now();

        doThrow(new AsylumCaseServiceResponseException("Test exception", new RuntimeException()))
            .when(scheduler).schedule(any(TimedEvent.class));

        // Should not throw exception
        scheduleTimedEventService.scheduleTimedEvent(caseId, scheduledDate, event);

        verify(scheduler).schedule(any(TimedEvent.class));
    }
}
