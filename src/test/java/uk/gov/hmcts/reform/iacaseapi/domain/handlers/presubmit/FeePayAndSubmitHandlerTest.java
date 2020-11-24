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
public class FeePayAndSubmitHandlerTest {

    @Mock private FeePayment<AsylumCase> feePayment;
    @Mock private Callback<AsylumCase> callback;

    private FeePayAndSubmitHandler feePayAndSubmitHandler;

    @Before
    public void setUp() {

        feePayAndSubmitHandler =
            new FeePayAndSubmitHandler(true, feePayment);
    }

    @Test
    public void should_make_feePayment_and_update_the_case() {

        Arrays.asList(
            Event.PAY_AND_SUBMIT_APPEAL
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(feePayment.aboutToSubmit(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePayAndSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);

            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    public void it_cannot_handle_callback_if_fee_payment_not_enabled() {

        FeePayAndSubmitHandler feePayAndSubmitHandlerWithDisabledPayment =
            new FeePayAndSubmitHandler(
                false,
                feePayment
            );

        assertThatThrownBy(() -> feePayAndSubmitHandlerWithDisabledPayment.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> feePayAndSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePayAndSubmitHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL) {

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

        feePayAndSubmitHandler =
            new FeePayAndSubmitHandler(false, feePayment);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = feePayAndSubmitHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feePayAndSubmitHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePayAndSubmitHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePayAndSubmitHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePayAndSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
