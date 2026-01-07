package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateStatutoryTimeframe24WeeksConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private UpdateStatutoryTimeframe24WeeksConfirmation updateStatutoryTimeframe24WeeksConfirmation =
        new UpdateStatutoryTimeframe24WeeksConfirmation();

    @Test
    void should_return_confirmation_when_adding() {

        when(callback.getEvent()).thenReturn(Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS);

        PostSubmitCallbackResponse callbackResponse =
            updateStatutoryTimeframe24WeeksConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have updated the statutory timeframe 24 weeks");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can review this status in the Overview tab.");

    }

    @Test
    void should_return_confirmation_when_removing() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);

        PostSubmitCallbackResponse callbackResponse =
            updateStatutoryTimeframe24WeeksConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have updated the statutory timeframe 24 weeks");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can review this status in the Overview tab.");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = updateStatutoryTimeframe24WeeksConfirmation.canHandle(callback);

            if (event == Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS || event == Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
