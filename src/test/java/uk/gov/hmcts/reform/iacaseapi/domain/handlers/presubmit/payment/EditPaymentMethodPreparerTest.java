package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EditPaymentMethodPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private EditPaymentMethodPreparer editPaymentMethodPreparer;

    @BeforeEach
    public void setUp() {

        editPaymentMethodPreparer = new EditPaymentMethodPreparer(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_PAYMENT_METHOD);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {
        "PAID", "PAYMENT_PENDING"
    })
    void should_throw_error_on_edit_payment_method_for_no_remission_non_failed_appeal_payments(PaymentStatus paymentStatus) {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You can only change the payment method to card following a failed payment using Payment by Account."));
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = {
        "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"
    })
    void should_throw_error_on_approved_remissions(RemissionType remissionType) {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You can only change the payment method to card following a failed payment using Payment by Account."));
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = {
        "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"
    })
    void should_throw_error_on_partially_approved_remissions(RemissionType remissionType) {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You can only change the payment method to card following a failed payment using Payment by Account."));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "RP", "EA", "HU"
    })
    void should_throw_error_on_inflight_appeals_without_failed_payment() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You can only change the payment method to card following a failed payment using Payment by Account."));
    }

    @Test
    void should_not_throw_error_on_edit_payment_method_for_failed_appeal_payment() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.FAILED));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(0, callbackResponse.getErrors().size());
    }

    @ParameterizedTest
    @EnumSource(value = RemissionType.class, names = {
        "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"
    })
    void should_not_throw_error_on_edit_payment_method_for_rejected_remissions(RemissionType remissionType) {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(0, callbackResponse.getErrors().size());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "RP", "EA", "HU"
    })
    void should_not_throw_error_on_inflight_appeals_with_failed_payment() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.FAILED));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isEmpty();
        assertEquals(0, callbackResponse.getErrors().size());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> editPaymentMethodPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editPaymentMethodPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editPaymentMethodPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editPaymentMethodPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = editPaymentMethodPreparer.canHandle(callbackStage, callback);

                if (event == Event.EDIT_PAYMENT_METHOD
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
