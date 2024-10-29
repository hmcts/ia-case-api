package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EndAppealConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @InjectMocks
    private EndAppealConfirmation endAppealConfirmation;

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PostSubmitCallbackResponse callbackResponse =
            endAppealConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have ended the appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Any hearings requested or listed in List Assist have been automatically cancelled.");
    }

    @Test
    void should_return_manual_hearing_confirmation_required() {

        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, String.class))
            .thenReturn(Optional.of(""));
        when(asylumCase.read(AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED))
            .thenReturn(Optional.of("Yes"));

        PostSubmitCallbackResponse callbackResponse =
            endAppealConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have ended the appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("[Cancel the hearing on the Hearings tab](/cases/case-details/0/hearings)");
    }

    @Test
    public void should_return_notification_failed_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, String.class))
            .thenReturn(Optional.of("FAIL"));

        PostSubmitCallbackResponse callbackResponse =
            endAppealConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isEmpty());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("![Respondent notification failed confirmation]"
                           + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next");
        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Contact the respondent to tell them what has changed, including any action they need to take.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> endAppealConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = endAppealConfirmation.canHandle(callback);

            if (event == Event.END_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> endAppealConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
