package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReviewHearingRequirementsConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    ReviewHearingRequirementsConfirmation reviewHearingRequirementsConfirmation;

    @BeforeEach
    void setUp() {

        reviewHearingRequirementsConfirmation =
            new ReviewHearingRequirementsConfirmation();
    }

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);

        PostSubmitCallbackResponse callbackResponse =
            reviewHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the agreed hearing adjustments");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.");
    }

    @Test
    void should_return_confirmation_when_list_case_without_requirements() {

        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);

        PostSubmitCallbackResponse callbackResponse =
            reviewHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the agreed hearing adjustments");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = reviewHearingRequirementsConfirmation.canHandle(callback);

            if (event == Event.REVIEW_HEARING_REQUIREMENTS || event == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS || event == Event.UPDATE_HEARING_ADJUSTMENTS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
