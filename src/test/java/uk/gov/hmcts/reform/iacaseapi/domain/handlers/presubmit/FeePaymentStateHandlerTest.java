package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentStateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private FeePayment<AsylumCase> feePayment;

    private FeePaymentStateHandler feePaymentStateHandler;

    @Before
    public void setUp() {

        feePaymentStateHandler =
            new FeePaymentStateHandler(true, feePayment);
    }

    @Test
    public void should_return_payment_pending_state_for_hu_appeal_type_and_payment_due_status() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PAYMENT_PENDING);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_same_state_for_hu_appeal_type_and_when_paid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_payment_pending_state_for_ea_appeal_type_and_payment_due_status() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PAYMENT_PENDING);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_same_state_for_ea_appeal_type_and_when_paid() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_appeal_started_state_for_pa_appeal_type_and_payment_due_status() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_same_state_for_successful_payment_of_pa_appeal_type() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(returnedCallbackResponse.getState(), State.CASE_BUILDING);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_pending_payment_state_for_failed_payment_of_ea_appeal_type() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.PAYMENT_PENDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.FAILED));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(returnedCallbackResponse.getState(), State.PAYMENT_PENDING);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
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
                    && (callback.getEvent() == Event.SUBMIT_APPEAL || callback.getEvent() == Event.PAYMENT_APPEAL)) {

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
            new FeePaymentStateHandler(false, feePayment);

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
