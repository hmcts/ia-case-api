package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentHandlerTest {

    @Mock private FeePayment<AsylumCase> feePayment;
    @Mock private Callback<AsylumCase> callback;

    private FeePaymentHandler feePaymentHandler;

    @Before
    public void setUp() {

        feePaymentHandler =
                new FeePaymentHandler(true, feePayment);
    }

    @Test
    public void should_make_feePayment_and_update_the_case() {

        Arrays.asList(
                Event.START_APPEAL
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
    public void it_cannot_handle_callback_if_feepayment_not_enabled() {

        FeePaymentHandler feePaymentHandlerWithDisabledPayment =
                new FeePaymentHandler(
                        false,
                        feePayment
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
                        || callback.getEvent() == Event.EDIT_APPEAL)) {

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
                new FeePaymentHandler(false, feePayment);

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
