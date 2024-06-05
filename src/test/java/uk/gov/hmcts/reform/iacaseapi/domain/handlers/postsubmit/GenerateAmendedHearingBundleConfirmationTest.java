package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GenerateAmendedHearingBundleConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private final GenerateAmendedHearingBundleConfirmation generateAmendedHearingBundleConfirmation =
        new GenerateAmendedHearingBundleConfirmation();

    @Test
    void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);

        PostSubmitCallbackResponse callbackResponse =
            generateAmendedHearingBundleConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());
        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("The amended hearing bundle is being generated");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("What happens next");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "You will soon be able to view the amended hearing bundle in the documents tab.</br>"
                    + "You and the other parties will be notified when the amended hearing bundle is available.</br>"
                    +
                    "If the bundle fails to generate, you will be notified."
            );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> generateAmendedHearingBundleConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = generateAmendedHearingBundleConfirmation.canHandle(callback);
            if (event == Event.GENERATE_AMENDED_HEARING_BUNDLE) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> generateAmendedHearingBundleConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateAmendedHearingBundleConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
