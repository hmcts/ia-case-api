package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class EjpTurnOnNotificationConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    private EjpTurnOnNotificationConfirmation ejpTurnOnNotificationConfirmation = new EjpTurnOnNotificationConfirmation();
    
    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.TURN_ON_NOTIFICATIONS);

        PostSubmitCallbackResponse callbackResponse =
                ejpTurnOnNotificationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# You have turned on notifications");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ejpTurnOnNotificationConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = ejpTurnOnNotificationConfirmation.canHandle(callback);

            if (event == Event.TURN_ON_NOTIFICATIONS) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ejpTurnOnNotificationConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpTurnOnNotificationConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_notification_failed_confirmation() {

        when(callback.getEvent()).thenReturn(Event.TURN_ON_NOTIFICATIONS);

        PostSubmitCallbackResponse callbackResponse =
                ejpTurnOnNotificationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("All parties will be notified that the case has been transferred to the First-tier Tribunal.");

        assertThat(callbackResponse.getConfirmationHeader().get())
                .contains("ou have turned on notifications");
    }



}
