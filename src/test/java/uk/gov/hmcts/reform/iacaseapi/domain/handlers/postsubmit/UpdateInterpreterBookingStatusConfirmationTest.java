package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.UpdateInterpreterBookingStatusConfirmation.HEARING_UPDATE_FAILED_CONFIRMATION_MESSAGE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class UpdateInterpreterBookingStatusConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private UpdateInterpreterBookingStatusConfirmation updateInterpreterBookingStatusConfirmation =
        new UpdateInterpreterBookingStatusConfirmation();

    private static final long caseId = 12345;
    private static final String confirmationText = "#### What happens next\n\n"
        + "The hearing has been updated with the interpreter booking status. This information is now visible in List Assist.<br><br>"
        + "Ensure that the [interpreter details](/case/IA/Asylum/" + caseId + "/trigger/updateInterpreterDetails)";

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_confirmation() {

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
    public void should_set_confirmation_body_when_manual_hearing_update_required() {

        when(asylumCase.read(MANUAL_UPDATE_HEARING_REQUIRED)).thenReturn(Optional.of(YES));

        PostSubmitCallbackResponse callbackResponse =
            updateInterpreterBookingStatusConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertEquals("", callbackResponse.getConfirmationHeader().get());
        assertEquals(HEARING_UPDATE_FAILED_CONFIRMATION_MESSAGE, callbackResponse.getConfirmationBody().get());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
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
