package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEW_TIME_EXTENSION_DECISION;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReviewTimeExtensionConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    ReviewTimeExtensionConfirmation reviewTimeExtensionConfirmation =
        new ReviewTimeExtensionConfirmation();

    @Test
    void should_return_confirmation_for_granted() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION)).thenReturn(Optional.of(TimeExtensionDecision.GRANTED));

        PostSubmitCallbackResponse callbackResponse =
            reviewTimeExtensionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have granted a time extension");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                           + "The appellant has been notified that their request has been "
                           + "granted"
                           + " and that they must submit their Appeal Reasons by the new due date.<br>"
                           + "You will be notified when it is ready to review.\n"
            );
    }

    @Test
    void should_return_confirmation_for_refused() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION)).thenReturn(Optional.of(TimeExtensionDecision.REFUSED));

        PostSubmitCallbackResponse callbackResponse =
            reviewTimeExtensionConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have refused a time extension");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                           + "The appellant has been notified that their request has been "
                           + "refused"
                           + " and that they must submit their Appeal Reasons by the new due date.<br>"
                           + "You will be notified when it is ready to review.\n"
            );
    }

    @Test
    void should_throw_exception_if_no_decision() {

        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewTimeExtensionConfirmation.handle(callback))
            .hasMessage("Cannot handle reviewTimeExtension without a decision")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewTimeExtensionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = reviewTimeExtensionConfirmation.canHandle(callback);

            if (event == Event.REVIEW_TIME_EXTENSION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewTimeExtensionConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewTimeExtensionConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
