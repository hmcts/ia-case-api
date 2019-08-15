package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
public class HearingAttendeesAndDurationRecordedConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private HearingAttendeesAndDurationRecordedConfirmation hearingAttendeesAndDurationRecordedConfirmation =
        new HearingAttendeesAndDurationRecordedConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.RECORD_ATTENDEES_AND_DURATION);

        PostSubmitCallbackResponse callbackResponse =
            hearingAttendeesAndDurationRecordedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You have recorded the attendees and duration of the hearing")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The judge will record the decision and reasons.<br>")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You don't need to do anything more with this case.")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hearingAttendeesAndDurationRecordedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = hearingAttendeesAndDurationRecordedConfirmation.canHandle(callback);

            if (event == Event.RECORD_ATTENDEES_AND_DURATION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> hearingAttendeesAndDurationRecordedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingAttendeesAndDurationRecordedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}