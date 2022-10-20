package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AipToLegalRepJourneyHandlerTest {

    private AipToLegalRepJourneyHandler aipToLegalRepJourneyHandler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    @BeforeEach
    public void setUp() throws Exception {
        aipToLegalRepJourneyHandler = new AipToLegalRepJourneyHandler();
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = aipToLegalRepJourneyHandler.canHandle(callbackStage, callback);

                if (event == Event.NOC_REQUEST
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void it_should_not_handle_callback_for_rep_journey() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = aipToLegalRepJourneyHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void it_should_convert_case_to_rep_journey() {

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        aipToLegalRepJourneyHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        verify(asylumCase, times(1)).remove(JOURNEY_TYPE.value());
    }

    @Test
    void state_is_reasonsForAppealSubmitted_transition_to_caseUnderReview() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.REASONS_FOR_APPEAL_SUBMITTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(response.getState(), State.CASE_UNDER_REVIEW);
    }

    @Test
    void state_is_awaitingReasonsForAppeal_transition_to_caseBuilding() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(response.getState(), State.CASE_BUILDING);
    }

    @Test
    void state_is_awaitingClarifyingQuestionsAnswers_transition_to_previousState() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(PRE_CLARIFYING_STATE, State.class)).thenReturn(Optional.of(State.APPEAL_SUBMITTED));

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.APPEAL_SUBMITTED, response.getState());
    }

    @Test
    void state_is_awaitingClarifyingQuestionsAnswers_transition_to_legal_rep_valid_state() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(PRE_CLARIFYING_STATE, State.class)).thenReturn(Optional.of(State.REASONS_FOR_APPEAL_SUBMITTED));

        PreSubmitCallbackResponse<AsylumCase> response = aipToLegalRepJourneyHandler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.CASE_UNDER_REVIEW, response.getState());
    }
}
