package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;



@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EditCaseListingConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private EditCaseListingConfirmation editCaseListingConfirmation =
        new EditCaseListingConfirmation();

    @Test
    void should_return_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);

        PostSubmitCallbackResponse callbackResponse =
            editCaseListingConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("The case has been re-listed");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("A new Notice of Hearing has been generated. All parties will be notified by email.<br>");

    }

    @Test
    void should_return_notification_failed_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_EDIT_LISTING_INSTRUCT_STATUS, String.class))
            .thenReturn(Optional.of("FAIL"));

        PostSubmitCallbackResponse callbackResponse =
            editCaseListingConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        Assertions.assertThat(callbackResponse.getConfirmationHeader()).isNotPresent();
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("![Respondent notification failed confirmation]"
                           + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)"
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Contact the respondent to tell them what has changed, including any action they need to take.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editCaseListingConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = editCaseListingConfirmation.canHandle(callback);

            if (event == Event.EDIT_CASE_LISTING) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> editCaseListingConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editCaseListingConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
