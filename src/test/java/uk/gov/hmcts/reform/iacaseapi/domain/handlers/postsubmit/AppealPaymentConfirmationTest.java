package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealPaymentConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private AppealPaymentConfirmation appealPaymentConfirmation = new AppealPaymentConfirmation();

    @Before
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_return_payment_success_confirmation_when_payment_paid() {

        when(asylumCase.read(FEE_AMOUNT, BigDecimal.class)).thenReturn(Optional.of(BigDecimal.valueOf(140.00)));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));

        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_STARTED);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You have paid for the appeal")
        );
        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You still need to submit it")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("Payment successful")
        );
    }

    @Test
    public void should_return_payment_success_confirmation_after_appeal_submitted() {

        when(asylumCase.read(FEE_AMOUNT, BigDecimal.class)).thenReturn(Optional.of(BigDecimal.valueOf(140.00)));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));

        when(callback.getCaseDetails().getState()).thenReturn(State.PAYMENT_PENDING);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You have paid for the appeal")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("Payment successful")
        );
    }

    @Test
    public void should_return_payment_failed_confirmation_when_payment_failed() {

        when(asylumCase.read(FEE_AMOUNT, BigDecimal.class)).thenReturn(Optional.of(BigDecimal.valueOf(140.00)));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(FAILED));
        when(asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class)).thenReturn(Optional.of("Your account is deleted"));

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> appealPaymentConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = appealPaymentConfirmation.canHandle(callback);

            if (event == Event.PAYMENT_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealPaymentConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealPaymentConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
