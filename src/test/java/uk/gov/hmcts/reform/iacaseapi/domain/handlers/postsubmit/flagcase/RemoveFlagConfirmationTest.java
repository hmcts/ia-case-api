package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(MockitoJUnitRunner.class)
public class RemoveFlagConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    private RemoveFlagConfirmation removeFlagConfirmation =
        new RemoveFlagConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_FLAG);

        PostSubmitCallbackResponse callbackResponse =
            removeFlagConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You've removed the flag from this case")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("What happens next")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(
                "This flag has been removed from the case. The case will proceed as usual."
            )
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> removeFlagConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = removeFlagConfirmation.canHandle(callback);

            if (event == Event.REMOVE_FLAG) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeFlagConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
