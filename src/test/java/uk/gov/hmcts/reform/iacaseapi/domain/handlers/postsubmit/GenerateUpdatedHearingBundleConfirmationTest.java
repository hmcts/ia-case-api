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
class GenerateUpdatedHearingBundleConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    private final GenerateUpdatedHearingBundleConfirmation generateUpdatedHearingBundleConfirmation =
        new GenerateUpdatedHearingBundleConfirmation();

    @Test
    void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);

        PostSubmitCallbackResponse callbackResponse =
            generateUpdatedHearingBundleConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());
        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("The hearing bundle is being generated");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("What happens next");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "You will soon be able to view the hearing bundle in the [Documents tab](/cases/case-details/1234#Documents) in your case details page.</br>"
                    + "All other parties will be notified when the hearing bundle is available.</br>"
                    + "If the bundle fails to generate, you will get a notification and you will need to try again."
            );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> generateUpdatedHearingBundleConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = generateUpdatedHearingBundleConfirmation.canHandle(callback);
            if (event == Event.GENERATE_UPDATED_HEARING_BUNDLE) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> generateUpdatedHearingBundleConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateUpdatedHearingBundleConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
