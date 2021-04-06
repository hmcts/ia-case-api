package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestFeeRemissionConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    private RequestFeeRemissionConfirmation requestFeeRemissionConfirmation;

    @BeforeEach
    void setUp() {

        requestFeeRemissionConfirmation = new RequestFeeRemissionConfirmation();
    }

    @Test
    void should_return_the_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        PostSubmitCallbackResponse callbackResponse =
            requestFeeRemissionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertSame("# Your remission request has been submitted", callbackResponse.getConfirmationHeader().get());
        assertSame("#### What happens next\n\n"
                      + "The Tribunal will review the remission request. The decision details will be available to view in the appeal tab "
                      + "when a decision has been made.<br>", callbackResponse.getConfirmationBody().get());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestFeeRemissionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = requestFeeRemissionConfirmation.canHandle(callback);

            if (event == Event.REQUEST_FEE_REMISSION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestFeeRemissionConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestFeeRemissionConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
