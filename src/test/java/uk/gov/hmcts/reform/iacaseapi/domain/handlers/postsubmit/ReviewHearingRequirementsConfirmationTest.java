package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Before;
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
public class ReviewHearingRequirementsConfirmationTest {

    @Mock private Callback<AsylumCase> callback;

    private ReviewHearingRequirementsConfirmation reviewHearingRequirementsConfirmation;

    @Before
    public void setUp() {
        reviewHearingRequirementsConfirmation =
            new ReviewHearingRequirementsConfirmation();
    }

    @Test
    public void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);

        PostSubmitCallbackResponse callbackResponse =
            reviewHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You've recorded the agreed hearing adjustments")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.")
        );
    }

    @Test
    public void should_return_confirmation_when_list_case_without_requirements() {

        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);

        PostSubmitCallbackResponse callbackResponse =
            reviewHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You've recorded the agreed hearing adjustments")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.")
        );
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = reviewHearingRequirementsConfirmation.canHandle(callback);

            if (event == Event.REVIEW_HEARING_REQUIREMENTS || event == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
