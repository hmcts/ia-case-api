package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateInterpreterDetailsConfirmationTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;
    @Mock
    private AsylumCase asylumCase;

    private UpdateInterpreterDetailsConfirmation interpreterDetailsConfirmation;

    private static final long caseId = 12345;
    private static final String autoHearingMessage = "#### What happens next\n\n"
            + "The hearing has been updated with the interpreter details. This information is now visible in List Assist.<br><br>"
            + "Ensure that the [interpreter booking status](/case/IA/Asylum/" + caseId + "/trigger/updateInterpreterBookingStatus)"
            + " is up to date.";
    private static final String originalMessage = "#### What happens next\n\n"
            + "You now need to update the hearing in the "
            + "[Hearings tab](/case/IA/Asylum/" + caseId + "#Hearing%20and%20appointment)"
            + " to ensure the new interpreter information is displayed in List Assist."
            + "\n\nIf updates need to be made to the interpreter booking status this should be completed"
            + " before updating the hearing.";

    @BeforeEach
    void setup() {
        interpreterDetailsConfirmation = new UpdateInterpreterDetailsConfirmation(locationBasedFeatureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    void should_return_confirmation(YesOrNo value) {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(value);


        PostSubmitCallbackResponse callbackResponse =
            interpreterDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# Interpreter details have been updated");

        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == NO) {
            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(originalMessage);
        } else {
            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(autoHearingMessage);
        }

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> interpreterDetailsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = interpreterDetailsConfirmation.canHandle(callback);

            if (event == Event.UPDATE_INTERPRETER_DETAILS) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> interpreterDetailsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> interpreterDetailsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}