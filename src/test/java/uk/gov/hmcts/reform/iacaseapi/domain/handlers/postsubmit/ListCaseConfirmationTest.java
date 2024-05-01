package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_HEARING_INSTRUCT_STATUS;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ListCaseConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;


    private ListCaseConfirmation listCaseConfirmation = new ListCaseConfirmation();

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "NO", "YES" })
    void should_return_confirmation(YesOrNo isAda) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        when(asylumCase.read(HOME_OFFICE_HEARING_INSTRUCT_STATUS, String.class))
                .thenReturn(Optional.of(" "));
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        PostSubmitCallbackResponse callbackResponse =
            listCaseConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        if (isAda.equals(YesOrNo.YES)) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("You have listed the case");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("The legal representative will be directed to submit the appellant's hearing<br>");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("requirements and a Notice of Hearing will be sent to all parties.");

        } else {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("You have listed the case");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("The hearing notice will be sent to all parties.<br>");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("You don't need to do any more on this case.");
        }

    }


    @Test
    void should_return_notification_failed_confirmation() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(asylumCase.read(HOME_OFFICE_HEARING_INSTRUCT_STATUS, String.class))
            .thenReturn(Optional.of("FAIL"));

        PostSubmitCallbackResponse callbackResponse =
            listCaseConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        Assertions.assertThat(callbackResponse.getConfirmationHeader()).isNotPresent();
        assertTrue(callbackResponse.getConfirmationBody().isPresent());


        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("![Respondent notification failed confirmation]"
                           + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Contact the respondent to tell them what has changed, including any action they need to take.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> listCaseConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = listCaseConfirmation.canHandle(callback);

            if (event == Event.LIST_CASE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> listCaseConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
