package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.REFUSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_TYPE;

import java.util.Optional;
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
public class RecordApplicationConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private RecordApplicationConfirmation recordApplicationConfirmation = new RecordApplicationConfirmation();

    private String granted = GRANTED.toString();
    private String refused = REFUSED.toString();
    private String editListing = TRANSFER.toString();
    private String changeDate = TIME_EXTENSION.toString();
    private String withdraw = WITHDRAW.toString();
    private String updateHearingRequirements = UPDATE_HEARING_REQUIREMENTS.toString();
    private long caseId = 1234;

    @Test
    public void should_return_confirmation_application_refused() {

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
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have recorded an application")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("A notification will be sent to both parties, informing them that an application was requested and refused. The case will progress as usual.")
        );

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    public void should_return_confirmation_application_edit_listing() {

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
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have recorded an application")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("The application decision has been recorded and is now available in the applications tab. Contact the listing team to relist the case. Once the case has been relisted, a new hearing notice will be issued.")
        );

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    public void should_return_confirmation_application_granted_change_date() {

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
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have recorded an application")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You must now [change the direction due date](/case/IA/Asylum/" + caseId + "/trigger/changeDirectionDueDate). You can also view the application decision in the Applications tab.")
        );

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    public void should_return_confirmation_application_granted_end_appeal() {

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
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have recorded an application")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You must now [end the appeal](/case/IA/Asylum/" + caseId + "/trigger/endAppeal).")
        );

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    public void should_return_confirmation_application_granted_update_hearing_requirements() {

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
            callbackResponse.getConfirmationHeader().get(),
            containsString("# You have recorded an application")
        );

        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString("You must now [update the hearing requirements](/case/IA/Asylum/" + caseId + "/trigger/updateHearingRequirements) based on the new information provided in the application. The application decision is available to view in the Application tab.")
        );

        verify(asylumCase).clear(APPLICATION_DECISION);
        verify(asylumCase).clear(APPLICATION_TYPE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordApplicationConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordApplicationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordApplicationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}