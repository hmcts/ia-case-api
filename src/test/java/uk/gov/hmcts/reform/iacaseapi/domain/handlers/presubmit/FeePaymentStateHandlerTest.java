package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.*;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentStateHandlerTest {

    @Mock private DateProvider dateProvider;
    @Mock private Scheduler scheduler;
    @Mock private FeatureToggler featureToggler;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    @Captor private ArgumentCaptor<TimedEvent> timedEventArgumentCaptor;

    private final boolean timedEventServiceEnabled = true;
    private int paymentDueInDays = 14;
    private LocalDate now = LocalDate.now();
    private String id = "someId";
    private long caseId = 12345;
    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private FeePaymentStateHandler feePaymentStateHandler;

    @Before
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(false);
        when(caseDetails.getId()).thenReturn(caseId);
        when(dateProvider.now()).thenReturn(now);

        feePaymentStateHandler =
            new FeePaymentStateHandler(
                true,
                timedEventServiceEnabled,
                paymentDueInDays,
                dateProvider,
                scheduler,
                featureToggler
            );
    }

    @Test
    public void should_return_payment_pending_state_for_hu_appeal_type_and_payment_due_status() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of("Payment due"));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PAYMENT_PENDING);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_same_state_for_hu_appeal_type_and_when_paid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of("Success"));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_payment_pending_state_for_ea_appeal_type_and_payment_due_status() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of("Payment due"));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        feePaymentStateHandler.triggerTimedEvent(asylumCase, callback);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PAYMENT_PENDING);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_same_state_for_ea_appeal_type_and_when_paid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of("Success"));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_appeal_started_state_for_pa_appeal_type_and_payment_due_status() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of("Payment due"));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_schedule_automatic_end_appeal_14_days_from_now() {

        TimedEvent timedEvent = new TimedEvent(
            id,
            Event.END_APPEAL,
            ZonedDateTime.of(now.plusDays(paymentDueInDays + 1), LocalTime.MIDNIGHT, ZoneId.systemDefault()),
            jurisdiction,
            caseType,
            caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(PAYMENT_STATUS, String.class);

        feePaymentStateHandler.triggerTimedEvent(asylumCase, callback);

        assertThat(asylumCase).isEqualTo(returnedCallbackResponse.getData());
        verify(asylumCase, times(1)).write(AUTOMATIC_END_APPEAL, id);
        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getScheduledDateTime(), result.getScheduledDateTime());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    public void should_schedule_automatic_end_appeal_in_10_minutes_for_test_user() {

        LocalDateTime nowWithTime = LocalDateTime.now();

        when(dateProvider.nowWithTime()).thenReturn(nowWithTime);

        when(featureToggler.getValue("timed-event-short-delay", false)).thenReturn(true);

        TimedEvent timedEvent = new TimedEvent(
            id,
            Event.END_APPEAL,
            ZonedDateTime.of(nowWithTime, ZoneId.systemDefault()).plusMinutes(5),
            jurisdiction,
            caseType,
            caseId
        );
        when(scheduler.schedule(any(TimedEvent.class))).thenReturn(timedEvent);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(PAYMENT_STATUS, String.class);

        feePaymentStateHandler.triggerTimedEvent(asylumCase, callback);

        assertThat(asylumCase).isEqualTo(returnedCallbackResponse.getData());
        verify(asylumCase, times(1)).write(AUTOMATIC_END_APPEAL, id);
        verify(scheduler).schedule(timedEventArgumentCaptor.capture());

        TimedEvent result = timedEventArgumentCaptor.getValue();

        assertEquals(timedEvent.getCaseId(), result.getCaseId());
        assertEquals(timedEvent.getJurisdiction(), result.getJurisdiction());
        assertEquals(timedEvent.getCaseType(), result.getCaseType());
        assertEquals(timedEvent.getScheduledDateTime(), result.getScheduledDateTime());
        assertEquals(timedEvent.getEvent(), result.getEvent());
        assertEquals("", result.getId());
    }

    @Test
    public void should_rethrow_exception_when_scheduler_failed() {

        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(AsylumCaseServiceResponseException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.triggerTimedEvent(asylumCase, callback))
            .isExactlyInstanceOf(AsylumCaseServiceResponseException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePaymentStateHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_fee_payment_not_enabled() {

        feePaymentStateHandler =
            new FeePaymentStateHandler(
                false,
                timedEventServiceEnabled,
                paymentDueInDays,
                dateProvider,
                scheduler,
                featureToggler);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePaymentStateHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feePaymentStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
