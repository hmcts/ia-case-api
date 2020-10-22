package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentStateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private FeePaymentStateHandler feePaymentStateHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        feePaymentStateHandler = new FeePaymentStateHandler(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
    }

    @Test
    public void should_return_updated_state_for_pa_successful_payAndSubmit_as_appeal_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAID));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_hu_successful_payAndSubmit_as_appeal_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAID));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_ea_successful_payAndSubmit_as_appeal_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAID));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_pa_failed_payAndSubmit_as_appeal_started_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.FAILED));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_hu_failed_payAndSubmit_as_appeal_started_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.FAILED));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_ea_failed_payAndSubmit_as_appeal_started_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.FAILED));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_pa_payment_pending_payAndSubmit_as_appeal_started_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_hu_payment_pending_payAndSubmit_as_appeal_started_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_ea_payment_pending_payAndSubmit_as_appeal_started_state() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_pa_payment_pending_submit_as_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_hu_payment_pending_submit_as_pending_payment_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_ea_payment_pending_submit_as_pending_payment_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    @Parameters({ "EA", "HU", "PA" })
    public void should_return_valid_state_on_having_remissions_for_given_appeal_types(String type) {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.valueOf(type)));
        asylumCase.write(REMISSION_TYPE, RemissionType.HO_WAIVER_REMISSION);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (Arrays.asList(AppealType.EA, AppealType.HU).contains(AppealType.valueOf(type))) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        }
    }

    @Test
    @Parameters({ "EA", "HU", "PA" })
    public void should_return_appeal_submitted_state_with_no_remission(String type) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.valueOf(type)));
        asylumCase.write(REMISSION_TYPE, RemissionType.NO_REMISSION);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        if (Arrays.asList(AppealType.EA, AppealType.HU).contains(AppealType.valueOf(type))) {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.PENDING_PAYMENT);
        } else {
            Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        }
    }

    @Test
    @Parameters({ "FAILED", "PAYMENT_PENDING" })
    public void should_return_current_state_for_failed_and_pending_payments(String paymentStatus) {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.valueOf(paymentStatus)));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));
        asylumCase.write(REMISSION_TYPE, RemissionType.NO_REMISSION);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
        assertEquals(State.APPEAL_STARTED, returnedCallbackResponse.getState());

    }

    @Test
    public void should_return_updated_state_for_dc_submit_as_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_PENDING));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.DC));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_updated_state_for_non_payment_rp_submit_as_submitted_state() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.RP));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_SUBMITTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_empty_appeal_type() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        AsylumCase asylumCase = new AsylumCase();
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal type is not present");
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

        verify(asylumCase, never()).read(PAYMENT_STATUS);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePaymentStateHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.PAY_AND_SUBMIT_APPEAL,
                    Event.SUBMIT_APPEAL
                ).contains(event)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feePaymentStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
