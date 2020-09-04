package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentStateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private FeePaymentStateHandler feePaymentStateHandler;

    @Before
    public void setUp() {
        feePaymentStateHandler = new FeePaymentStateHandler(true);
    }

    @Test
    public void should_return_updated_state_for_successful_pa_payment_as_appeal_submitted_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

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
    public void should_return_updated_state_for_successful_hu_payment_as_appeal_submitted_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

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
    public void should_return_updated_state_for_successful_ea_payment_as_appeal_submitted_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

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
    public void should_return_current_state_for_failed_pa_payment_as_appeal_started_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_DUE));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_failed_hu_payment_as_appeal_started_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_DUE));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.HU));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
    }

    @Test
    public void should_return_current_state_for_failed_ea_payment_as_appeal_started_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, Optional.of(PaymentStatus.PAYMENT_DUE));
        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.EA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            feePaymentStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        Assertions.assertThat(returnedCallbackResponse.getState()).isEqualTo(State.APPEAL_STARTED);
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

        verify(asylumCase, never()).read(PAYMENT_STATUS);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePaymentStateHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.PAY_AND_SUBMIT_APPEAL
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
