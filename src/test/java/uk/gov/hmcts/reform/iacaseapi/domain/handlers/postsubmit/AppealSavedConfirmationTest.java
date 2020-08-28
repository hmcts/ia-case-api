package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAY_FOR_THE_APPEAL_OPTION;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealSavedConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    AppealSavedConfirmation appealSavedConfirmation =
        new AppealSavedConfirmation();

    @Test
    void should_return_confirmation() {

        long caseId = 1234;
        when(asylumCase.read(PAY_FOR_THE_APPEAL_OPTION, String.class)).thenReturn(Optional.of(""));

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        PostSubmitCallbackResponse callbackResponse =
                appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal details have been saved");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("### Do this next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("[submit your appeal](/case/IA/Asylum/" + caseId + "/trigger/submitAppeal)");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("Not ready to submit yet?");
    }

    @Test
    void should_return_confirmation_for_pay_for() {

        long caseId = 1234;
        AppealType appealType = AppealType.HU;

        when(asylumCase.read(PAY_FOR_THE_APPEAL_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(appealType));

        PostSubmitCallbackResponse callbackResponse =
                appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
                callbackResponse.getConfirmationHeader().get())
                .contains("# Your appeal details have been saved");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("### Do this next");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("[pay for and submit your appeal](/case/IA/Asylum/" + caseId + "/trigger/paymentAppeal)");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("Not ready to submit yet?");
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealSavedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = appealSavedConfirmation.canHandle(callback);

            if (event == Event.START_APPEAL || event == Event.EDIT_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSavedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSavedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
