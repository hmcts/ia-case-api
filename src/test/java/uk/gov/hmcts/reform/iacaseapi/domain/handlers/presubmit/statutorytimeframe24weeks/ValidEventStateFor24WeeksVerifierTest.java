package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_INTERPRETER_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks.ValidEventStateFor24WeeksVerifier.INVALID_UPDATE_HEARING_REQUEST_STATES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ValidEventStateFor24WeeksVerifierTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ValidEventStateFor24WeeksVerifier validEventStateFor24WeeksVerifier;

    @BeforeEach
    public void setUp() {
        validEventStateFor24WeeksVerifier = new ValidEventStateFor24WeeksVerifier();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void can_handle_throws_exception_for_null_arguments() {
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
            validEventStateFor24WeeksVerifier.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception = assertThrows(NullPointerException.class, () ->
            validEventStateFor24WeeksVerifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUEST",
        "UPDATE_INTERPRETER_DETAILS",
        "UPDATE_INTERPRETER_BOOKING_STATUS",
        "ADJOURN_HEARING_WITHOUT_DATE",
        "RECORD_ADJOURNMENT_DETAILS",
        "UPDATE_HEARING_REQUIREMENTS",
        "REVIEW_HEARING_REQUIREMENTS"
    })
    void can_handle_returns_true_for_valid_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(validEventStateFor24WeeksVerifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUEST",
        "UPDATE_INTERPRETER_DETAILS",
        "UPDATE_INTERPRETER_BOOKING_STATUS",
        "ADJOURN_HEARING_WITHOUT_DATE",
        "RECORD_ADJOURNMENT_DETAILS",
        "UPDATE_HEARING_REQUIREMENTS",
        "REVIEW_HEARING_REQUIREMENTS"
    }, mode = EnumSource.Mode.EXCLUDE)
    void can_handle_returns_false_for_invalid_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(validEventStateFor24WeeksVerifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_START"}, mode = EnumSource.Mode.EXCLUDE)
    void can_handle_returns_false_for_invalid_callback_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(UPDATE_INTERPRETER_DETAILS);
        assertFalse(validEventStateFor24WeeksVerifier.canHandle(stage, callback));
    }

    @Test
    void handle_throws_exception_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            validEventStateFor24WeeksVerifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "AWAITING_CLARIFYING_QUESTIONS_ANSWERS",
        "CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED"
    }, mode = EnumSource.Mode.EXCLUDE)
    void getEffectiveState_returns_current_state_if_not_clarifying_questions() {
        when(callback.getCaseDetails().getState()).thenReturn(State.SUBMIT_HEARING_REQUIREMENTS);
        assertEquals(State.SUBMIT_HEARING_REQUIREMENTS,
            validEventStateFor24WeeksVerifier.getEffectiveState(callback, asylumCase));
        verify(asylumCase, never()).read(AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "AWAITING_CLARIFYING_QUESTIONS_ANSWERS",
        "CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED"
    })
    void getEffectiveState_returns_pre_clarifying_state_if_clarifying_questions(State state) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state);
        when(asylumCase.read(AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE, State.class))
            .thenReturn(Optional.of(State.CASE_UNDER_REVIEW));
        assertEquals(State.CASE_UNDER_REVIEW,
            validEventStateFor24WeeksVerifier.getEffectiveState(callback, asylumCase));
    }

    @ParameterizedTest
    @MethodSource("invalidStatesForUpdateHearingRequest")
    void isInvalidState_returns_true_for_invalid_states_update_hearing_request_non_24w(State state) {
        assertTrue(validEventStateFor24WeeksVerifier.isInvalidState(Event.UPDATE_HEARING_REQUEST, state, false));
    }

    @ParameterizedTest
    @MethodSource("validStatesForUpdateHearingRequest")
    void isInvalidState_returns_false_for_valid_states_update_hearing_request_non_24w(State state) {
        assertFalse(validEventStateFor24WeeksVerifier.isInvalidState(Event.UPDATE_HEARING_REQUEST, state, false));
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void isInvalidState_returns_false_for_all_states_update_hearing_request_24w(State state) {
        assertFalse(validEventStateFor24WeeksVerifier.isInvalidState(Event.UPDATE_HEARING_REQUEST, state, true));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"LISTING"}, mode = EnumSource.Mode.EXCLUDE)
    void isInvalidState_returns_false_for_all_states_non_listing_any_event_24w(State state) {
        assertFalse(validEventStateFor24WeeksVerifier.isInvalidState(UPDATE_INTERPRETER_DETAILS, state, true));
    }

    @Test
    void isInvalidState_returns_true_for_listing_24w() {
        assertTrue(validEventStateFor24WeeksVerifier.isInvalidState(UPDATE_INTERPRETER_DETAILS, State.LISTING, true));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"RESPONDENT_REVIEW"}, mode = EnumSource.Mode.EXCLUDE)
    void isInvalidState_returns_false_for_all_states_non_respondent_review_any_event_24w(State state) {
        assertFalse(validEventStateFor24WeeksVerifier.isInvalidState(UPDATE_INTERPRETER_DETAILS, state, false));
    }

    @Test
    void isInvalidState_returns_true_for_respondent_review_non_24w() {
        assertTrue(validEventStateFor24WeeksVerifier.isInvalidState(UPDATE_INTERPRETER_DETAILS, State.RESPONDENT_REVIEW, false));
    }

    @Test
    void handle_with_no_error_if_valid_state() {
        when(callback.getEvent()).thenReturn(UPDATE_INTERPRETER_DETAILS);
        when(callback.getCaseDetails().getState()).thenReturn(State.SUBMIT_HEARING_REQUIREMENTS);
        PreSubmitCallbackResponse<AsylumCase> response = validEventStateFor24WeeksVerifier
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void handle_with_error_if_invalid_state() {
        when(callback.getEvent()).thenReturn(UPDATE_INTERPRETER_DETAILS);
        when(callback.getCaseDetails().getState()).thenReturn(State.LISTING);
        when(asylumCase.read(AsylumCaseFieldDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> response = validEventStateFor24WeeksVerifier
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertFalse(response.getErrors().isEmpty());
        assertEquals(1, response.getErrors().size());
        assertEquals("This event cannot be run in this state", response.getErrors().iterator().next());
    }

    static Stream<State> invalidStatesForUpdateHearingRequest() {
        return INVALID_UPDATE_HEARING_REQUEST_STATES.stream();
    }

    static Stream<State> validStatesForUpdateHearingRequest() {
        return Arrays.stream(State.values())
            .filter(state -> !INVALID_UPDATE_HEARING_REQUEST_STATES.contains(state));
    }
}
