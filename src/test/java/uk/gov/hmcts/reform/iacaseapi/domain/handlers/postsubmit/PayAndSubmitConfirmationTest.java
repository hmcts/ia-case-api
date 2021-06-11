package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.payment.PayAndSubmitConfirmation;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PayAndSubmitConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeePayment<AsylumCase> feePayment;
    @Mock
    private PostNotificationSender<AsylumCase> postNotificationSender;
    @Mock
    private Scheduler scheduler;
    @Mock
    private DateProvider dateProvider;

    @Captor
    private ArgumentCaptor<TimedEvent> timedEventCaptor;

    private AppealPaymentConfirmationProvider appealPaymentConfirmationProvider =
        new AppealPaymentConfirmationProvider();

    private LocalDateTime dateTime = LocalDateTime.now();

    private long caseId = 1234;

    private PayAndSubmitConfirmation payAndSubmitConfirmation;

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        payAndSubmitConfirmation =
            new PayAndSubmitConfirmation(
                appealPaymentConfirmationProvider,
                feePayment,
                postNotificationSender,
                scheduler,
                dateProvider
            );
    }

    @Test
    void should_return_success_payment_confirmation() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());

        PostSubmitCallbackResponse equalCallbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(equalCallbackResponse);
        assertTrue(equalCallbackResponse.getConfirmationHeader().isPresent());
        assertTrue(equalCallbackResponse.getConfirmationBody().isPresent());

        assertThat(
            equalCallbackResponse.getConfirmationHeader().get())
            .contains("# Your appeal has been paid for and submitted");

        assertThat(
            equalCallbackResponse.getConfirmationBody().get())
            .contains("### What happens next");

        assertThat(
            equalCallbackResponse.getConfirmationBody().get())
            .contains("You will receive an email confirming that this appeal has been submitted successfully.");
    }

    @Test
    void should_return_payment_failed_confirmation_when_payment_failed() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(FAILED));
        when(asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class)).thenReturn(Optional.of("Your account is deleted"));
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());
        when(dateProvider.nowWithTime()).thenReturn(dateTime);

        PostSubmitCallbackResponse callbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.");

        verify(scheduler).schedule(timedEventCaptor.capture());
        assertEquals(timedEventCaptor.getValue().getId(), "");
        assertEquals(timedEventCaptor.getValue().getJurisdiction(), "IA");
        assertEquals(timedEventCaptor.getValue().getCaseType(), "Asylum");
        assertEquals(timedEventCaptor.getValue().getEvent(), Event.MOVE_TO_PAYMENT_PENDING);
        assertEquals(timedEventCaptor.getValue().getScheduledDateTime(), dateTime.atZone(ZoneId.systemDefault()));
        assertEquals(timedEventCaptor.getValue().getCaseId(), caseId);

        verify(postNotificationSender).send(any(Callback.class));
    }

    @Test
    void should_rollback_only_status_when_payment_appeal_event_and_payment_failed() {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(FAILED));
        when(asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class)).thenReturn(Optional.of("Your account is deleted"));
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());
        when(dateProvider.nowWithTime()).thenReturn(dateTime);

        PostSubmitCallbackResponse callbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.");

        verify(scheduler).schedule(timedEventCaptor.capture());
        assertEquals(timedEventCaptor.getValue().getId(), "");
        assertEquals(timedEventCaptor.getValue().getJurisdiction(), "IA");
        assertEquals(timedEventCaptor.getValue().getCaseType(), "Asylum");
        assertEquals(timedEventCaptor.getValue().getEvent(), Event.ROLLBACK_PAYMENT);
        assertEquals(timedEventCaptor.getValue().getScheduledDateTime(), dateTime.atZone(ZoneId.systemDefault()));
        assertEquals(timedEventCaptor.getValue().getCaseId(), caseId);

        verify(postNotificationSender).send(any(Callback.class));
    }

    @Test
    void should_return_payment_failed_confirmation_when_payment_timeout() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(feePayment.aboutToSubmit(callback)).thenThrow(new RuntimeException("error"));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(FAILED));
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());
        when(dateProvider.nowWithTime()).thenReturn(dateTime);

        PostSubmitCallbackResponse callbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.");

        verify(scheduler).schedule(timedEventCaptor.capture());
        assertEquals(timedEventCaptor.getValue().getId(), "");
        assertEquals(timedEventCaptor.getValue().getJurisdiction(), "IA");
        assertEquals(timedEventCaptor.getValue().getCaseType(), "Asylum");
        assertEquals(timedEventCaptor.getValue().getEvent(), Event.MOVE_TO_PAYMENT_PENDING);
        assertEquals(timedEventCaptor.getValue().getScheduledDateTime(), dateTime.atZone(ZoneId.systemDefault()));
        assertEquals(timedEventCaptor.getValue().getCaseId(), caseId);

        verify(postNotificationSender).send(any(Callback.class));
    }

    @Test
    void should_return_payment_failed_confirmation_when_payment_failed_and_rollback_failed() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(FAILED));
        when(asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class)).thenReturn(Optional.of("Your account is deleted"));
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());
        when(dateProvider.nowWithTime()).thenReturn(dateTime);
        when(scheduler.schedule(any(TimedEvent.class))).thenThrow(new RuntimeException("error"));
        PostSubmitCallbackResponse callbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.");

        verify(postNotificationSender).send(any(Callback.class));
    }

    @Test
    void should_return_out_of_time_confirmation_when_out_of_time() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());

        PostSubmitCallbackResponse callbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimePaidConfirmation.png)\n");
    }

    @Test
    void should_return_correct_confirmation_when_notifications_failed() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(postNotificationSender.send(any(Callback.class))).thenThrow(new RuntimeException("error"));

        PostSubmitCallbackResponse callbackResponse =
            payAndSubmitConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimePaidConfirmation.png)\n");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> payAndSubmitConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = payAndSubmitConfirmation.canHandle(callback);

            if (event == Event.PAY_AND_SUBMIT_APPEAL
                || event == Event.PAY_FOR_APPEAL
                || event == Event.PAYMENT_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> payAndSubmitConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> payAndSubmitConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
