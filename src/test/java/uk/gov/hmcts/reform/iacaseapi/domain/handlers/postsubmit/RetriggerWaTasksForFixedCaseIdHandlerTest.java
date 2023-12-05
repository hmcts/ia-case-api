package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class RetriggerWaTasksForFixedCaseIdHandlerTest {


    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Captor
    private ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;

    private boolean timedEventServiceEnabled = true;
    private LocalDateTime now = LocalDateTime.now();
    private String timedEventId = "";
    private long caseId = Long.parseLong("1677132005196104");
    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private RetriggerWaTasksForFixedCaseIdHandler retriggerWaTasksForFixedCaseIdHandler;
    private RetriggerWaTasksForFixedCaseIdHandler retriggerWaTasksForFixedCaseIdHandlerDisabled;

    @BeforeEach
    public void setUp() {

        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        timedEventServiceEnabled,
                        dateProvider,
                        scheduler
                );
    }

    @Test
    void handle_callback_should_return_expected() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_TASKS_BULK);
        boolean canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callback);
        assertThat(canHandle).isEqualTo(true);

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callback);
        assertThat(canHandle).isEqualTo(false);
    }

    @Test
    void handle_callback_should_return_false_timed_event_service_disabled() {
        retriggerWaTasksForFixedCaseIdHandlerDisabled =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        false,
                        dateProvider,
                        scheduler
                );
        boolean canHandle = retriggerWaTasksForFixedCaseIdHandlerDisabled.canHandle(callback);
        assertThat(canHandle).isEqualTo(false);
    }

    @Test
    void handling_should_throw_if_cannot_handle() {
        assertThatThrownBy(
                () -> retriggerWaTasksForFixedCaseIdHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_schedule_anything_when_read_json_fails()  {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_TASKS_BULK);
        when(dateProvider.nowWithTime()).thenReturn(now);
        retriggerWaTasksForFixedCaseIdHandler.setFilePath("/");
        retriggerWaTasksForFixedCaseIdHandler.handle(callback);
        verify(scheduler, times(0)).schedule(timedEventArgumentCaptor.capture());
    }

    @Test
    void should_not_schedule_anything_when_no_case_ids()  {
        String testFilePath = "/retriggerWaTasksEmptyCaseList.json";
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_TASKS_BULK);
        when(dateProvider.nowWithTime()).thenReturn(now);
        retriggerWaTasksForFixedCaseIdHandler.setFilePath(testFilePath);
        retriggerWaTasksForFixedCaseIdHandler.handle(callback);
        verify(scheduler, times(0)).schedule(timedEventArgumentCaptor.capture());
    }

    @Test
    void should_schedule_re_trigger_wa_tasks_5_minutes_in_future_for_all_case_ids() {

        String testFilePath = "/retriggerWaTasksCaseList.json";
        ZonedDateTime timeToSchedule = ZonedDateTime.of(now, ZoneId.systemDefault()).plusMinutes(5);
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_TASKS_BULK);
        when(dateProvider.nowWithTime()).thenReturn(now);

        retriggerWaTasksForFixedCaseIdHandler.setFilePath(testFilePath);
        retriggerWaTasksForFixedCaseIdHandler.handle(callback);
        verify(scheduler, times(10)).schedule(timedEventArgumentCaptor.capture());

        TimedEvent finalResult = timedEventArgumentCaptor.getValue();
        List<TimedEvent> timedEventList = timedEventArgumentCaptor.getAllValues();
        TimedEvent expectedFinalTimedEvent = new TimedEvent(
                timedEventId,
                Event.RE_TRIGGER_WA_TASKS,
                timeToSchedule,
                jurisdiction,
                caseType,
                caseId
        );
        assertEquals(expectedFinalTimedEvent.getCaseId(), finalResult.getCaseId());
        assertEquals(expectedFinalTimedEvent.getJurisdiction(), finalResult.getJurisdiction());
        assertEquals(expectedFinalTimedEvent.getCaseType(), finalResult.getCaseType());
        assertEquals(expectedFinalTimedEvent.getEvent(), finalResult.getEvent());
        assertEquals(expectedFinalTimedEvent.getId(), finalResult.getId());
        assertEquals(expectedFinalTimedEvent.getScheduledDateTime(), finalResult.getScheduledDateTime());

        List<Long> timedEventListCaseIds = timedEventList.stream()
                .map(TimedEvent::getCaseId)
                .collect(Collectors.toList());
        List<Long> expectedCaseIds = Arrays.asList(
                Long.parseLong("5260728023204485"),
                Long.parseLong("7829484608979593"),
                Long.parseLong("3007004947258233"),
                Long.parseLong("4719620009252072"),
                Long.parseLong("6797092066725243"),
                Long.parseLong("9301281768878771"),
                Long.parseLong("8509676174519453"),
                Long.parseLong("1682542357170697"),
                Long.parseLong("3673342967892569"),
                Long.parseLong("1677132005196104")
        );

        assertEquals(expectedCaseIds, timedEventListCaseIds);
    }
}
