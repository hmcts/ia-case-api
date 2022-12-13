package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
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
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PayAndSubmitConfirmationTest {

    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;
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
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        payAndSubmitConfirmation =
            new PayAndSubmitConfirmation(
                appealPaymentConfirmationProvider,
                feePayment,
                postNotificationSender,
                scheduler,
                dateProvider,
                ccdSupplementaryUpdater
            );
    }

    @Test
    void should_invoke_supplementary_updater() {

        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(postNotificationSender.send(any(Callback.class))).thenReturn(new PostSubmitCallbackResponse());

        payAndSubmitConfirmation.handle(callback);

        verify(ccdSupplementaryUpdater).setHmctsServiceIdSupplementary(callback);
    }

    @Test
    void should_rollback_only_status_when_payment_appeal_event_and_payment_failed() {

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
        verify(ccdSupplementaryUpdater).setHmctsServiceIdSupplementary(callback);
        assertEquals(timedEventCaptor.getValue().getId(), "");
        assertEquals(timedEventCaptor.getValue().getJurisdiction(), "IA");
        assertEquals(timedEventCaptor.getValue().getCaseType(), "Asylum");
        assertEquals(timedEventCaptor.getValue().getEvent(), Event.ROLLBACK_PAYMENT);
        assertEquals(timedEventCaptor.getValue().getScheduledDateTime(), dateTime.atZone(ZoneId.systemDefault()));
        assertEquals(timedEventCaptor.getValue().getCaseId(), caseId);

        verify(postNotificationSender).send(any(Callback.class));
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

            if (event == Event.PAYMENT_APPEAL) {

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
