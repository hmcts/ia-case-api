package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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
public class EndApplicationConfirmationTest {
    @Mock
    private Callback<BailCase> callback;

    private final EndApplicationConfirmation endApplicationConfirmation = new EndApplicationConfirmation();

    @Test
    void should_handle_only_valid_event() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = endApplicationConfirmation.canHandle(callback);
            if (event.equals(Event.END_APPLICATION)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_throw_null_args() {
        assertThatThrownBy(() -> endApplicationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> endApplicationConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_set_confirmation_header_body() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        PostSubmitCallbackResponse response = endApplicationConfirmation
            .handle(callback);
        assertEquals("# You have ended the application", response.getConfirmationHeader().get());
        assertThat(response.getConfirmationBody().get()).contains("#### What happens next\n\n");
        assertThat(response.getConfirmationBody().get())
            .contains("A notification has been sent to all parties. No further action is required.");
    }

}
