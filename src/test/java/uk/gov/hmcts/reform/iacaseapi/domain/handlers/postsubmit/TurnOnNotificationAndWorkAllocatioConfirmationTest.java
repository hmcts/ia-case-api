package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class TurnOnNotificationAndWorkAllocatioConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private TurnOnNotificationAndWorkAllocatioConfirmation turnOnNotificationAndWorkAllocatioConfirmation;

    @BeforeEach
    public void setUp() {
        turnOnNotificationAndWorkAllocatioConfirmation = new TurnOnNotificationAndWorkAllocatioConfirmation();
    }

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.TURN_ON_NOTIFICATIONS_WA_TASKS);

        PostSubmitCallbackResponse callbackResponse =
            turnOnNotificationAndWorkAllocatioConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have turned on notifications/WA tasks");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The rehydration process has now been completed and the case can be progressed.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> turnOnNotificationAndWorkAllocatioConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void it_can_handle_callback(Event event) {

        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = turnOnNotificationAndWorkAllocatioConfirmation.canHandle(callback);

        if (event == Event.TURN_ON_NOTIFICATIONS_WA_TASKS) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> turnOnNotificationAndWorkAllocatioConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> turnOnNotificationAndWorkAllocatioConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

