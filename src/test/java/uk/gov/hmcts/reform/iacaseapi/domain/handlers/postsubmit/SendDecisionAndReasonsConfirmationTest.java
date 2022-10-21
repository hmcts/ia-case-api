package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDecisionAndReasonsConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private TTL ttl;
    @Mock private TimeToLiveDataService timeToLiveDataService;

    private SendDecisionAndReasonsConfirmation sendDecisionAndReasonsConfirmation;

    @BeforeEach
    void setup() {
        sendDecisionAndReasonsConfirmation = new SendDecisionAndReasonsConfirmation(timeToLiveDataService);
    }

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));
        when(ttl.getSuspended()).thenReturn(YesOrNo.YES);
        when(caseDetails.getState()).thenReturn(State.DECIDED);

        PostSubmitCallbackResponse callbackResponse =
            sendDecisionAndReasonsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You've uploaded the Decision and Reasons document");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Both parties have been notified of the decision. They'll also be able to access the Decision and Reasons document from the Documents tab.");

        verify(timeToLiveDataService, times(1)).updateTheClock(callback, false);
    }

    @Test
    void should_not_trigger_manageCaseTtl_when_unsuccessful() {

        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.ENDED); // unexpected state (unsuccessful)

        PostSubmitCallbackResponse callbackResponse =
            sendDecisionAndReasonsConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(timeToLiveDataService, never()).updateTheClock(callback, false);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDecisionAndReasonsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = sendDecisionAndReasonsConfirmation.canHandle(callback);

            if (event == Event.SEND_DECISION_AND_REASONS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendDecisionAndReasonsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
