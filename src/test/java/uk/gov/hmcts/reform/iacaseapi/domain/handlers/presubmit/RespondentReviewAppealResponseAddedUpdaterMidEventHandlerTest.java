package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME_REASON;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RespondentReviewAppealResponseAddedUpdaterMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private RespondentReviewAppealResponseAddedUpdaterMidEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondentReviewAppealResponseAddedUpdaterMidEventHandler();
    }

    @Test
    void should_write_end_appeal_outcome_reason_if_withdrawn() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of("WITHDRAWN"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase).write(eq(END_APPEAL_OUTCOME_REASON), contains("The Respondent has withdrawn"));
    }

    @Test
    void should_not_write_end_appeal_outcome_reason_if_not_withdrawn() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of("ALLOWED"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(response);
        verify(asylumCase, never()).write(eq(END_APPEAL_OUTCOME_REASON), any());
    }

    @Test
    void it_can_handle_callback_for_mid_event_and_end_appeal_event_only() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");

        assertThatThrownBy(() -> handler.handle(null, callback))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_if_cannot_handle() {

        assertThatThrownBy(() ->
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }
}