package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.COMPLETE_CLARIFY_QUESTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.NOC_REQUEST;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CompleteClarifyingQuestionsHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Captor private ArgumentCaptor<List<IdValue<ClarifyingQuestionAnswer>>> answersCaptor;

    private CompleteClarifyingQuestionsHandler handler;

    @BeforeEach
    public void setup() {
        handler = new CompleteClarifyingQuestionsHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
    }

    @Test
    void should_set_default_answer_to_clarifying_questions_for_event_completeClarifyQuestions() {
        should_set_default_answer_to_clarifying_questions(COMPLETE_CLARIFY_QUESTIONS,
                "No answer submitted because the question was marked as complete by the Tribunal");
    }

    @Test
    void should_set_default_answer_to_clarifying_questions_for_event_nocRequest() {
        should_set_default_answer_to_clarifying_questions(NOC_REQUEST,
                "No answer submitted because the question was marked as complete due to change in representation");
    }

    private void should_set_default_answer_to_clarifying_questions(Event event, String expectedAnswer) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails().getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);
        List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.singletonList(
            new IdValue<>("1", new ClarifyingQuestion("question 1"))
        );
        IdValue<Direction> directionWithQuestions = createDirectionWithQuestion("1", clarifyingQuestions, "uniqueId");
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(
            Optional.of(Collections.singletonList(directionWithQuestions)));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(asylumCase).isEqualTo(returnedCallbackResponse.getData());
        verify(asylumCase).write(eq(CLARIFYING_QUESTIONS_ANSWERS), answersCaptor.capture());

        List<IdValue<ClarifyingQuestionAnswer>> answers = answersCaptor.getValue();
        assertEquals(1, answers.size());
        assertEquals(expectedAnswer,
            answers.get(0).getValue().getAnswer());
    }

    @Test
    void should_not_set_default_answer_to_answered_clarifying_questions_for_event_complete_clarifying_questions() {
        should_not_set_default_answer_to_answered_clarifying_questions(COMPLETE_CLARIFY_QUESTIONS);
    }

    @Test
    void should_not_set_default_answer_to_answered_clarifying_questions_for_event_nocRequest() {
        should_not_set_default_answer_to_answered_clarifying_questions(NOC_REQUEST);
    }

    private void should_not_set_default_answer_to_answered_clarifying_questions(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails().getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);

        List<IdValue<ClarifyingQuestion>> clarifyingQuestions1 = List.of(
                new IdValue<>("q1", new ClarifyingQuestion("question 1"))
        );
        IdValue<Direction> directionWithQuestions1 = createDirectionWithQuestion(
                "1", clarifyingQuestions1, "uniqueId1");

        ClarifyingQuestionAnswer clarifyingQuestionAnswer = new ClarifyingQuestionAnswer(
                "2020-02-01",
                "2020-03-01",
                "2020-02-16",
                "question 1",
                "answer 1",
                "uniqueId1",
                Collections.emptyList()
        );
        List<IdValue<ClarifyingQuestionAnswer>> clarifyingQuestionAnswers =
                Collections.singletonList(new IdValue<>("q1", clarifyingQuestionAnswer));

        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS))
                .thenReturn(Optional.of(Collections.singletonList(directionWithQuestions1)));
        when(asylumCase.read(CLARIFYING_QUESTIONS_ANSWERS)).thenReturn(Optional.of(clarifyingQuestionAnswers));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(asylumCase).isEqualTo(returnedCallbackResponse.getData());
        verify(asylumCase).write(eq(CLARIFYING_QUESTIONS_ANSWERS), answersCaptor.capture());

        List<IdValue<ClarifyingQuestionAnswer>> answers = answersCaptor.getValue();
        assertEquals(1, answers.size());
        assertEquals(clarifyingQuestionAnswer.getAnswer(), answers.get(0).getValue().getAnswer());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "NOC_REQUEST", "COMPLETE_CLARIFY_QUESTIONS"
    })
    void should_set_expected_answers_to_clarifying_question_answers_for_answered_and_unanswered_questions(
            Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails().getState()).thenReturn(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS);

        List<IdValue<ClarifyingQuestion>> answeredClarifyingQuestions1 = List.of(
            new IdValue<>("q1", new ClarifyingQuestion("question 1"))
        );
        List<IdValue<ClarifyingQuestion>> clarifyingQuestions2 = List.of(
            new IdValue<>("q2", new ClarifyingQuestion("question 2"))
        );

        IdValue<Direction> directionWithQuestions1 = createDirectionWithQuestion(
            "1", answeredClarifyingQuestions1, "uniqueId1");
        IdValue<Direction> directionWithQuestions2 = createDirectionWithQuestion(
            "2", clarifyingQuestions2, "uniqueId2");
        List<IdValue<Direction>> directionWithQuestions = List.of(directionWithQuestions2, directionWithQuestions1);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of(directionWithQuestions));

        ClarifyingQuestionAnswer clarifyingQuestionAnswer1 = new ClarifyingQuestionAnswer(
            "2020-02-01",
            "2020-03-01",
            "2020-02-16",
            "question 1",
            "answer 1",
            "uniqueId1",
            Collections.emptyList()
        );
        List<IdValue<ClarifyingQuestionAnswer>> clarifyingQuestionAnswers =
                Collections.singletonList(new IdValue<>("q1", clarifyingQuestionAnswer1));

        when(asylumCase.read(CLARIFYING_QUESTIONS_ANSWERS)).thenReturn(Optional.of(clarifyingQuestionAnswers));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(returnedCallbackResponse);
        assertThat(asylumCase).isEqualTo(returnedCallbackResponse.getData());
        verify(asylumCase).write(eq(CLARIFYING_QUESTIONS_ANSWERS), answersCaptor.capture());

        List<IdValue<ClarifyingQuestionAnswer>> answers = answersCaptor.getValue();
        assertEquals(2, answers.size());

        String defaultAnswer = NOC_REQUEST.equals(event)
                ? "No answer submitted because the question was marked as complete due to change in representation"
                : "No answer submitted because the question was marked as complete by the Tribunal";

        // unanswered direction with clarifying question 2
        assertEquals(clarifyingQuestions2.get(0).getValue().getQuestion(), answers.get(0).getValue().getQuestion());
        assertEquals(defaultAnswer, answers.get(0).getValue().getAnswer());

        // answered direction with clarifying question 1
        assertEquals(
                answeredClarifyingQuestions1.get(0).getValue().getQuestion(), answers.get(1).getValue().getQuestion());
        assertEquals(clarifyingQuestionAnswer1.getAnswer(), answers.get(1).getValue().getAnswer());
    }

    @NotNull
    private IdValue<Direction> createDirectionWithQuestion(
            String id, List<IdValue<ClarifyingQuestion>> clarifyingQuestions1, String uniqueId) {
        return new IdValue<>(
                id,
                new Direction(
                        "You need to answer some questions about your appeal.",
                        Parties.APPELLANT,
                        "2020-02-16",
                        "2020-02-02",
                        DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
                        Collections.emptyList(),
                        clarifyingQuestions1,
                        uniqueId,
                        "directionType"
                )
        );
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (event == COMPLETE_CLARIFY_QUESTIONS || event == NOC_REQUEST)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_throw_exceptions_for_error_scenarios() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


}
