package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ApplyForCostsConfirmationTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private ApplyForCostsConfirmation applyForCostsConfirmation =
            new ApplyForCostsConfirmation();

    @Test
    void should_return_confirmation() {
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        PostSubmitCallbackResponse callbackResponse =
                applyForCostsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("You've made a costs application");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains(
                        "## What happens next\n\n"
                                + "Both you and the other party will receive an email notification confirming your application.\n\n"
                                + "The other party has 14 days to respond to the claim.\n\n"
                                + "If you have requested a hearing, the Tribunal will consider your request.\n\n"
                                + "You can review the details of your application in the [Costs tab](/cases/case-details/" + callback.getCaseDetails().getId() + "#Costs). ");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> applyForCostsConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = applyForCostsConfirmation.canHandle(callback);

            if (event == Event.APPLY_FOR_COSTS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> applyForCostsConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}