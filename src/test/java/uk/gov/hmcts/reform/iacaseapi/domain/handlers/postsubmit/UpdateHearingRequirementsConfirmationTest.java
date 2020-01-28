package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class UpdateHearingRequirementsConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private UpdateHearingRequirementsConfirmation updateHearingRequirementsConfirmation =
        new UpdateHearingRequirementsConfirmation();

    @Test
    public void should_return_confirmation() {

        long caseId = 12345;
        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        PostSubmitCallbackResponse callbackResponse =
            updateHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You've updated the hearing requirements")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You must now [update the hearing adjustments or confirm they haven't changed.](/case/IA/Asylum/" + caseId + "/trigger/updateHearingAdjustments)")
        );

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateHearingRequirementsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = updateHearingRequirementsConfirmation.canHandle(callback);

            if (event == Event.UPDATE_HEARING_REQUIREMENTS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateHearingRequirementsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateHearingRequirementsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
