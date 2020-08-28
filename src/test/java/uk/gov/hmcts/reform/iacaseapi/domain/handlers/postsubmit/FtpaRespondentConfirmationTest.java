package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaRespondentConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    FtpaRespondentConfirmation ftpaRespondentConfirmation =
        new FtpaRespondentConfirmation();

    @Test
    void should_return_success_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(eq(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME), eq(YesOrNo.class))).thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            ftpaRespondentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've applied for permission to appeal to the Upper Tribunal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The First-tier Tribunal will review your application and decide shortly.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }

    @Test
    void should_return_success_confirmation_when_flag_is_empty() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PostSubmitCallbackResponse callbackResponse =
            ftpaRespondentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've applied for permission to appeal to the Upper Tribunal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The First-tier Tribunal will review your application and decide shortly.<br>");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");
    }


    @Test
    void should_return_out_of_time_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(eq(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME), eq(YesOrNo.class))).thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            ftpaRespondentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The First-tier Tribunal will consider the reasons it has been submitted out of time. If the Tribunal accepts your reasons,"
                           + " it will consider your application and make a decision shortly.<br>");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaRespondentConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = ftpaRespondentConfirmation.canHandle(callback);

            if (event == Event.APPLY_FOR_FTPA_RESPONDENT) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ftpaRespondentConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
