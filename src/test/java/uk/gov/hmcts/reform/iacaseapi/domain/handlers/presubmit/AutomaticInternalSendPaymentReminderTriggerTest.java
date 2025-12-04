package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class AutomaticInternalSendPaymentReminderTriggerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;
    @Captor
    private ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;
    private final LocalDateTime now = LocalDateTime.now();
    private final String id = "someId";
    private final long caseId = 12345;
    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";
    private final int sevenDaysAsMinutes = 10080;

    private AutomaticInternalSendPaymentReminderTrigger automaticInternalSendPaymentReminderTrigger;

    @BeforeEach
    void setUp() {

        automaticInternalSendPaymentReminderTrigger =
            new AutomaticInternalSendPaymentReminderTrigger(
                dateProvider,
                scheduler
            );
    }

    @Test
    void handling_should_throw_if_null_callback() {

        assertThatThrownBy(() -> automaticInternalSendPaymentReminderTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> automaticInternalSendPaymentReminderTrigger.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_can_not_handle() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_PAYMENT_STATUS); // unqualified event
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> automaticInternalSendPaymentReminderTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_schedule_send_payment_reminder_event_7_days_from_now() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(dateProvider.nowWithTime()).thenReturn(now);

        TimedEvent timedEvent = new TimedEvent(
            id,
            Event.SEND_PAYMENT_REMINDER_NOTIFICATION,
            ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(sevenDaysAsMinutes),
            jurisdiction,
            caseType,
            caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            automaticInternalSendPaymentReminderTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());
        verify(asylumCase, times(1)).write(AUTOMATIC_SEND_PAYMENT_REMINDER_TIMED_EVENT_ID, id);
        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    void should_not_schedule_payment_reminder_for_rehydrated_appeal() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
                .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class))
                .thenReturn(Optional.of(SourceOfAppeal.REHYDRATED_APPEAL));

        assertThatThrownBy(() ->
                automaticInternalSendPaymentReminderTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        )
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        verifyNoInteractions(scheduler);
    }

}
