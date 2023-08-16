package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

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
class UpdateInterpreterBookingStatusConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private UpdateInterpreterBookingStatusConfirmation updateInterpreterBookingStatusConfirmation =
        new UpdateInterpreterBookingStatusConfirmation();

    private static final long caseId = 12345;
    private static final String confirmationText = "#### What happens next\n\n"
        + "You now need to update the hearing in the "
        + "[Hearings tab](/case/IA/Asylum/" + caseId + "#hearing)"
        + " to ensure the update is displayed in List Assist."
        + "\n\nIf an interpreter status has been moved to booked, or has been cancelled,"
        + " ensure that the interpreter details are up to date before updating the hearing.";
    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);


        PostSubmitCallbackResponse callbackResponse =
            updateInterpreterBookingStatusConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# Booking statuses have been updated");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(confirmationText);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateInterpreterBookingStatusConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = updateInterpreterBookingStatusConfirmation.canHandle(callback);

            if (event == Event.UPDATE_INTERPRETER_BOOKING_STATUS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateInterpreterBookingStatusConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateInterpreterBookingStatusConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}