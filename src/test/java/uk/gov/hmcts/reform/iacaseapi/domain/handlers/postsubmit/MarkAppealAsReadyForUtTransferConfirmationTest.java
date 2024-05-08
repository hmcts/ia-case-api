package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
class MarkAppealAsReadyForUtTransferConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    private MarkAsReadyForUtTransferConfirmation markAsReadyForUtTransferConfirmation = new MarkAsReadyForUtTransferConfirmation();

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.MARK_AS_READY_FOR_UT_TRANSFER);
        PostSubmitCallbackResponse callbackResponse = markAsReadyForUtTransferConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You've marked this appeal as ready to be transferred to the Upper Tribunal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("All parties have been notified. The Upper Tribunal will transfer the appeal.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> markAsReadyForUtTransferConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = markAsReadyForUtTransferConfirmation.canHandle(callback);
            if (event == Event.MARK_AS_READY_FOR_UT_TRANSFER) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAsReadyForUtTransferConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAsReadyForUtTransferConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
