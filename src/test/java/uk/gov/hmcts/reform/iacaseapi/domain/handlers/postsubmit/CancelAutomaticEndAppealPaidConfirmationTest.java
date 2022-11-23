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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CancelAutomaticEndAppealPaidConfirmationTest {


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
    private String id = "someId";
    private String timedEventId = "1234567";
    private long caseId = 12345;
    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private CancelAutomaticEndAppealPaidConfirmation cancelAutomaticEndAppealHandler;

    @BeforeEach
    public void setUp() {

        cancelAutomaticEndAppealHandler =
                new CancelAutomaticEndAppealPaidConfirmation(
                        timedEventServiceEnabled,
                        dateProvider,
                        scheduler
                );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_schedule_automatic_end_appeal_100_years_from_now() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_PAYMENT_STATUS);


        TimedEvent timedEvent = new TimedEvent(
                timedEventId,
                Event.END_APPEAL_AUTOMATICALLY,
                ZonedDateTime.of(now, ZoneId.systemDefault()).plusMinutes(52560000),
                jurisdiction,
                caseType,
                caseId
        );
        when(asylumCase.read(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID))
                .thenReturn(Optional.of(timedEventId));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        when(dateProvider.nowWithTime()).thenReturn(now);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        PostSubmitCallbackResponse callbackResponse =
                cancelAutomaticEndAppealHandler.handle(callback);

        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals(timedEvent.getId(), result.getId());
        assertEquals(timedEvent.getScheduledDateTime(), result.getScheduledDateTime());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
                () -> cancelAutomaticEndAppealHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.UPDATE_PAYMENT_STATUS);
        assertThatThrownBy(
                () -> cancelAutomaticEndAppealHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        assertThatThrownBy(
                () -> cancelAutomaticEndAppealHandler.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            for(PaymentStatus paymentStatus : PaymentStatus.values()){

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);

                when(callback.getEvent()).thenReturn(event);
                when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
                when(asylumCase.read(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID)).thenReturn(Optional.of("1234567"));


                boolean canHandle = cancelAutomaticEndAppealHandler.canHandle(callback);

                if (timedEventServiceEnabled
                        && event == Event.UPDATE_PAYMENT_STATUS
                        && paymentStatus == PaymentStatus.PAID
                        && !timedEventId.isEmpty() && timedEventId != null) {
                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }

                reset(callback);
            }
        }
    }

}
