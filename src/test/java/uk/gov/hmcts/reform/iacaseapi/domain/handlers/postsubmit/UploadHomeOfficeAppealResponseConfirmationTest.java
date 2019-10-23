package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
public class UploadHomeOfficeAppealResponseConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    private UploadHomeOfficeAppealResponseConfirmation uploadHomeOfficeAppealResponseConfirmation =
        new UploadHomeOfficeAppealResponseConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE);

        PostSubmitCallbackResponse callbackResponse =
            uploadHomeOfficeAppealResponseConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You've uploaded the appeal response")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The Tribunal will: \n* check that the Home Office response complies with the Procedure Rules and Practice Directions\n* inform you of any issues\n\nProviding there are no issues, the response will be shared with the appellant")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("All parties will be notified when the Hearing Notice is ready.")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> uploadHomeOfficeAppealResponseConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = uploadHomeOfficeAppealResponseConfirmation.canHandle(callback);

            if (event == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadHomeOfficeAppealResponseConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadHomeOfficeAppealResponseConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
