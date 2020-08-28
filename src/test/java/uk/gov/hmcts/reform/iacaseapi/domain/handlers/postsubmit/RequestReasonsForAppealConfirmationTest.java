package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

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
@SuppressWarnings("unchecked")
public class RequestReasonsForAppealConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    private RequestReasonsForAppealConfirmation requestReasonsForAppealConfirmation =
            new RequestReasonsForAppealConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_REASONS_FOR_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
                requestReasonsForAppealConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get(),
                containsString("You have sent a direction")
        );

        assertThat(
                callbackResponse.getConfirmationBody().get(),
                containsString("The appellant will be directed to submit their Appeal Reasons. You will be notified when it is ready to review.")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestReasonsForAppealConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = requestReasonsForAppealConfirmation.canHandle(callback);

            if (event == Event.REQUEST_REASONS_FOR_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestReasonsForAppealConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestReasonsForAppealConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
