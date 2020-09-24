package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_OFFLINE_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentHandlerTest {

    @Mock private FeePayment<AsylumCase> feePayment;
    @Mock private Callback<AsylumCase> callback;
    @Mock private FeePaymentDisplayProvider feePaymentDisplayProvider;

    private FeePaymentHandler feePaymentHandler;

    @Before
    public void setUp() {

        feePaymentHandler =
            new FeePaymentHandler(true, feePayment, feePaymentDisplayProvider);
    }

    @Test
    public void should_make_feePayment_and_update_the_case() {

        Arrays.asList(
            Event.PAYMENT_APPEAL
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(feePayment.aboutToSubmit(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);

            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    public void should_write_pending_for_pa_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(feePayment.aboutToSubmit(callback)).thenReturn(expectedUpdatedCase);
            when(expectedUpdatedCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.PA));
            when(expectedUpdatedCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,
                String.class)).thenReturn(Optional.of("payOffline"));


            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(expectedUpdatedCase, times(1))
                .write(PAYMENT_OFFLINE_FOR_DISPLAY, "Payment pending");
            verify(expectedUpdatedCase, times(1))
                .write(IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);

            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    public void should_write_pending_for_hu_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(feePayment.aboutToSubmit(callback)).thenReturn(expectedUpdatedCase);
            when(expectedUpdatedCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.HU));
            when(expectedUpdatedCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,
                String.class)).thenReturn(Optional.of("payOffline"));


            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(expectedUpdatedCase, times(1))
                .write(PAYMENT_OFFLINE_FOR_DISPLAY, "Payment pending");

            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    public void should_write_pending_for_ea_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(feePayment.aboutToSubmit(callback)).thenReturn(expectedUpdatedCase);
            when(expectedUpdatedCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.EA));
            when(expectedUpdatedCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,
                String.class)).thenReturn(Optional.of("payOffline"));


            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(expectedUpdatedCase, times(1))
                .write(PAYMENT_OFFLINE_FOR_DISPLAY, "Payment pending");

            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    public void it_cannot_handle_callback_if_feepayment_not_enabled() {

        FeePaymentHandler feePaymentHandlerWithDisabledPayment =
            new FeePaymentHandler(
                false,
                feePayment,
                feePaymentDisplayProvider
            );

        assertThatThrownBy(() -> feePaymentHandlerWithDisabledPayment.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePaymentHandler.canHandle(callbackStage, callback);

                if ((callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    && (callback.getEvent() == Event.START_APPEAL
                        || callback.getEvent() == Event.EDIT_APPEAL
                        || callback.getEvent() == Event.PAYMENT_APPEAL)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_feePayment_not_enabled() {

        feePaymentHandler =
            new FeePaymentHandler(false, feePayment, feePaymentDisplayProvider);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = feePaymentHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feePaymentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
