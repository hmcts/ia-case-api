package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class ForceCaseProgressionToHearingConfirmationTest {
    private static final String CONFIRMATION_BODY_LEGAL_REP = """
            #### What happens next

            The respondent and legal representative will be notified by email.""";
    private static final String CONFIRMATION_BODY_NO_LEGAL_REP = """
            #### What happens next

            The respondent will be notified by email.""";
    @Mock
    private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;

    private ForceCaseProgressionToHearingConfirmation forceCaseProgressionToHearingConfirmation =
        new ForceCaseProgressionToHearingConfirmation();

    @Test
    void given_respondent_is_represented_should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_HEARING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            forceCaseProgressionToHearingConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have forced the case progression to Hearing");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(CONFIRMATION_BODY_LEGAL_REP);
    }

    @Test
    void given_respondent_is_unrepresented_should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_HEARING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            forceCaseProgressionToHearingConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have forced the case progression to Hearing");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(CONFIRMATION_BODY_NO_LEGAL_REP);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> forceCaseProgressionToHearingConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = forceCaseProgressionToHearingConfirmation.canHandle(callback);

            if (event == Event.FORCE_CASE_TO_HEARING) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> forceCaseProgressionToHearingConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseProgressionToHearingConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
