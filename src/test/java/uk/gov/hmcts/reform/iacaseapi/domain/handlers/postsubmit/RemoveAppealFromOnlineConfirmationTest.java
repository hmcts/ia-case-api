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
public class RemoveAppealFromOnlineConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private RemoveAppealFromOnlineConfirmation removeAppealFromOnlineConfirmation = new RemoveAppealFromOnlineConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_APPEAL_FROM_ONLINE);

        PostSubmitCallbackResponse callbackResponse =
            removeAppealFromOnlineConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You've removed this appeal from the online service")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(
                "## Do this next\n"
                + "You now need to:</br>"
                + "1.Contact the appellant and the respondent to inform them that the case will proceed offline.</br>"
                + "2.Save all files associated with the appeal to the shared drive.</br>"
                + "3.Email a link to the saved files with the appeal reference number to: BAUArnhemHouse@justice.gov.uk"
            )
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> removeAppealFromOnlineConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = removeAppealFromOnlineConfirmation.canHandle(callback);

            if (event == Event.REMOVE_APPEAL_FROM_ONLINE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeAppealFromOnlineConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeAppealFromOnlineConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
