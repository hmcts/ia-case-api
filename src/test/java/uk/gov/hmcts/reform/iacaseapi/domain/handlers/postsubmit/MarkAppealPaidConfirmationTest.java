package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
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
public class MarkAppealPaidConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    private MarkAppealPaidConfirmation markAppealPaidConfirmation
        = new MarkAppealPaidConfirmation();

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.MARK_APPEAL_PAID);

        PostSubmitCallbackResponse callbackResponse =
            markAppealPaidConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("# Your have marked the appeal as paid")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("#### What happens next\n\n")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The Tribunal will be notified that the fee has been paid. The appeal will progress as usual.")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> markAppealPaidConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = markAppealPaidConfirmation.canHandle(callback);

            if (event == Event.MARK_APPEAL_PAID) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAppealPaidConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealPaidConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
