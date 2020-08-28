package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.REFUSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_TYPE;

import java.util.Optional;
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
class RecordApplicationConfirmationTest {

    static final String YOU_VE_RECORDED_AN_APPLICATION = "# You've recorded an application";
    @Mock private
    Callback<AsylumCase> callback;

    @Mock private
    CaseDetails<AsylumCase> caseDetails;

    @Mock private
    AsylumCase asylumCase;

    RecordApplicationConfirmation recordApplicationConfirmation = new RecordApplicationConfirmation();

    String granted = GRANTED.toString();
    String refused = REFUSED.toString();
    String editListing = TRANSFER.toString();
    String changeDate = TIME_EXTENSION.toString();
    String withdraw = WITHDRAW.toString();
    String updateHearingRequirements = UPDATE_HEARING_REQUIREMENTS.toString();
    String changeHearingCentre = CHANGE_HEARING_CENTRE.toString();
    String editAppealAfterSubmit = EDIT_APPEAL_AFTER_SUBMIT.toString();
    long caseId = 1234;

    @Test
    void should_return_confirmation_application_refused() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(refused));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(editListing));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("A notification will be sent to both parties, informing them that an application was requested and refused. The case will progress as usual.");

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void should_return_confirmation_application_edit_listing() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(granted));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(editListing));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The application decision has been recorded and is now available in the applications tab. Contact the listing team to relist the case. Once the case has been relisted, a new hearing notice will be issued.");

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void should_return_confirmation_application_granted_change_date() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(granted));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(changeDate));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You must now [change the direction due date](/case/IA/Asylum/" + caseId + "/trigger/changeDirectionDueDate). You can also view the application decision in the Applications tab.");

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void should_return_confirmation_application_granted_end_appeal() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(granted));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(withdraw));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You must now [end the appeal](/case/IA/Asylum/" + caseId + "/trigger/endAppeal).");

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void should_return_confirmation_application_granted_update_hearing_requirements() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(granted));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(updateHearingRequirements));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You must now [update the hearing requirements](/case/IA/Asylum/" + caseId + "/trigger/updateHearingRequirements) based on the new information provided in the application. The application decision is available to view in the Application tab.");

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void should_return_confirmation_application_granted_change_hearing_centre() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(granted));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(changeHearingCentre));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You must now [change the designated hearing centre](/case/IA/Asylum/" + caseId + "/trigger/changeHearingCentre) based on the new information provided in the application. The application decision is available to view in the Application tab.");

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void should_return_confirmation_application_granted_edit_appeal_after_submit() {

        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(granted));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(editAppealAfterSubmit));

        PostSubmitCallbackResponse callbackResponse =
            recordApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(YOU_VE_RECORDED_AN_APPLICATION);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The application decision has been recorded and is available in the applications tab. You must now [edit the appeal details](/case/IA/Asylum/" + caseId +  "/trigger/editAppealAfterSubmit) based on the new information provided in the application.");


        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordApplicationConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = recordApplicationConfirmation.canHandle(callback);

            if (event == Event.RECORD_APPLICATION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordApplicationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordApplicationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}