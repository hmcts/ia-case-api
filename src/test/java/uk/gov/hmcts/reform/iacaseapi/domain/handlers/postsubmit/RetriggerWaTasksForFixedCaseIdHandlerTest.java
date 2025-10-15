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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ScheduleTimedEventService;
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
    private ScheduleTimedEventService scheduleTimedEventService;
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
    private PreSubmitCallbackStage callbackStage = PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    private RetriggerWaTasksForFixedCaseIdHandler retriggerWaTasksForFixedCaseIdHandler;

    @BeforeEach
    public void setUp() {
        retriggerWaTasksForFixedCaseIdHandler = new RetriggerWaTasksForFixedCaseIdHandler(
                true,
                "/retriggerWaTasksCaseList.json",
                dateProvider,
                scheduleTimedEventService
        );
    }

    @Test
    void handle_callback_should_return_expected() {
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        boolean canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callbackStage, callback);
        assertThat(canHandle).isEqualTo(true);

        canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertThat(canHandle).isEqualTo(false);

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callbackStage, callback);
        assertThat(canHandle).isEqualTo(false);
    }

    @Test
    void handle_callback_should_return_false_timed_event_service_disabled() {
        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        false,
                        "/retriggerWaTasksCaseList.json",
                        dateProvider,
                        scheduleTimedEventService
                );
        boolean canHandle = retriggerWaTasksForFixedCaseIdHandler.canHandle(callbackStage, callback);
        assertThat(canHandle).isEqualTo(false);
    }

    @Test
    void handling_should_throw_if_cannot_handle() {
        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        timedEventServiceEnabled,
                        "/caseIdForRetrigger.json",
                        dateProvider,
                        scheduleTimedEventService
                );
        assertThatThrownBy(
                () -> retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_null_callback() {
        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        timedEventServiceEnabled,
                        "/retriggerWaTasksCaseList.json",
                        dateProvider,
                        scheduleTimedEventService
                );
        assertThatThrownBy(
                () -> retriggerWaTasksForFixedCaseIdHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(
                () -> retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_schedule_anything_when_read_json_fails()  {
        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        timedEventServiceEnabled,
                        "/",
                        dateProvider,
                        scheduleTimedEventService
                );
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.nowWithTime()).thenReturn(now);
        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);
        verify(scheduleTimedEventService, times(0)).scheduleTimedEvent(anyString(), any(ZonedDateTime.class), any(Event.class));
    }

    @Test
    void should_not_schedule_anything_when_no_case_ids()  {
        String testFilePath = "/retriggerWaTasksEmptyCaseList.json";
        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        timedEventServiceEnabled,
                        testFilePath,
                        dateProvider,
                        scheduleTimedEventService
                );
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.nowWithTime()).thenReturn(now);
        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);
        verify(scheduleTimedEventService, times(0)).scheduleTimedEvent(anyString(), any(ZonedDateTime.class), any(Event.class));
    }

    @Test
    void should_schedule_re_trigger_wa_tasks_5_minutes_in_future_for_all_case_ids() {

        String testFilePath = "/retriggerWaTasksCaseList.json";
        retriggerWaTasksForFixedCaseIdHandler =
                new RetriggerWaTasksForFixedCaseIdHandler(
                        timedEventServiceEnabled,
                        testFilePath,
                        dateProvider,
                        scheduleTimedEventService
                );
        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.nowWithTime()).thenReturn(now);

        retriggerWaTasksForFixedCaseIdHandler.handle(callbackStage, callback);
        verify(scheduleTimedEventService, times(10)).scheduleTimedEvent(anyString(), any(ZonedDateTime.class), eq(Event.RE_TRIGGER_WA_TASKS));

        ZonedDateTime timeToSchedule = ZonedDateTime.of(now, ZoneId.systemDefault()).plusMinutes(5);
        TimedEvent finalResult = timedEventArgumentCaptor.getValue();
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

        List<TimedEvent> timedEventList = timedEventArgumentCaptor.getAllValues();
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
