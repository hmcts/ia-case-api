package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTOMATIC_REMISSION_REMINDER_LEGAL_REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CancelAutomaticPaymentReminderRemissionLegalRepTriggerTest {

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
    private LocalDate now = LocalDate.now();
    private String timedEventId = "1234567";
    private long caseId = 12345;
    private String jurisdiction = "IA";
    private String caseType = "Asylum";
    private CancelAutomaticPaymentReminderRemissionLegalRepTrigger cancelAutomaticPaymentReminderRemissionLegalRepTrigger;

    @BeforeEach
    public void setUp() {

        cancelAutomaticPaymentReminderRemissionLegalRepTrigger =
            new CancelAutomaticPaymentReminderRemissionLegalRepTrigger(
                timedEventServiceEnabled,
                dateProvider,
                scheduler
            );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_schedule_automatic_direction_100_years_from_now() {
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        TimedEvent timedEvent = new TimedEvent(
            timedEventId,
            Event.RECORD_REMISSION_REMINDER,
            ZonedDateTime.of(now.plusDays(52560000), LocalTime.MIDNIGHT, ZoneId.systemDefault()),
            jurisdiction,
            caseType,
            caseId
        );
        when(asylumCase.read(AUTOMATIC_REMISSION_REMINDER_LEGAL_REP))
            .thenReturn(Optional.of(timedEventId));
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        when(callback.getCaseDetails().getId()).thenReturn(12345L);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            cancelAutomaticPaymentReminderRemissionLegalRepTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(asylumCase).isEqualTo(callbackResponse.getData());


        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals(timedEvent.getId(), result.getId());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> cancelAutomaticPaymentReminderRemissionLegalRepTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(
            () -> cancelAutomaticPaymentReminderRemissionLegalRepTrigger.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = cancelAutomaticPaymentReminderRemissionLegalRepTrigger.canHandle(callbackStage, callback);

                assertThat(canHandle).isEqualTo(timedEventServiceEnabled
                                                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                                && callback.getEvent() == Event.RECORD_REMISSION_DECISION);
            }

            reset(callback);
        }
    }

}