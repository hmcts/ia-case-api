package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ImaStatusConfirmationTest {

    @Mock
    private Callback<BailCase> callback;

    private final ImaStatusConfirmation imaStatusConfirmation =
        new ImaStatusConfirmation();

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.IMA_STATUS);

        PostSubmitCallbackResponse callbackResponse =
            imaStatusConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("IMA status updated");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "## What happens next\n\n"
                + "No further action is required.\n\n");

    }

    @Test
    void should_not_handle_invalid_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        AssertionsForClassTypes.assertThatThrownBy(() -> imaStatusConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_handle_only_valid_event() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = imaStatusConfirmation.canHandle(callback);
            if (event.equals(Event.IMA_STATUS)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }


    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> imaStatusConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> imaStatusConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
