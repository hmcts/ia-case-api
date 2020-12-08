package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordRemissionDecisionConfirmationTest {

    @Mock private AsylumCase asylumCase;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private Callback<AsylumCase> callback;

    @InjectMocks
    private RecordRemissionDecisionConfirmation recordRemissionDecisionConfirmation
        = new RecordRemissionDecisionConfirmation();


    @Test
    void handling_should_return_confirmation_for_remission_approved() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));

        PostSubmitCallbackResponse callbackResponse = recordRemissionDecisionConfirmation.handle(callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getConfirmationHeader()).isPresent();
        assertThat(callbackResponse.getConfirmationBody()).isPresent();

        assertThat(callbackResponse.getConfirmationHeader()).contains("# You have approved this remission application");
        assertThat(callbackResponse.getConfirmationBody())
            .contains("#### What happens next\n\n"
                      + "The appellant will be notified that you have approved this remission application. "
                      + "The appeal will progress as usual.<br>");
    }

    @Test
    void handling_should_return_confirmation_for_remission_partially_approved() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        PostSubmitCallbackResponse callbackResponse =
            recordRemissionDecisionConfirmation.handle(callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getConfirmationHeader()).isPresent();
        assertThat(callbackResponse.getConfirmationBody()).isPresent();

        assertThat(callbackResponse.getConfirmationHeader()).contains("# You have partially approved this remission application");
        assertThat(callbackResponse.getConfirmationBody())
            .contains("#### What happens next\n\n"
                      + "The appellant will be notified that they need to pay the outstanding fee. "
                      + "Once payment is made you will need to mark the appeal as paid.<br>");
    }

    @Test
    void handling_should_return_confirmation_for_remission_rejected() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PostSubmitCallbackResponse callbackResponse =
            recordRemissionDecisionConfirmation.handle(callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getConfirmationHeader()).isPresent();
        assertThat(callbackResponse.getConfirmationBody()).isPresent();

        assertThat(callbackResponse.getConfirmationHeader()).contains("# You have rejected this remission application");
        assertThat(callbackResponse.getConfirmationBody())
            .contains("#### What happens next\n\n"
                      + "The appellant will be notified that they must pay the full fee for this appeal.<br>");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordRemissionDecisionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = recordRemissionDecisionConfirmation.canHandle(callback);

            if (event == Event.RECORD_REMISSION_DECISION) {

                assertThat(canHandle).isTrue();
            } else {
                assertThat(canHandle).isFalse();
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordRemissionDecisionConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
