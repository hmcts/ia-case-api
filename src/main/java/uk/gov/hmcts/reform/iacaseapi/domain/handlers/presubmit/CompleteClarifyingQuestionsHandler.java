package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS;

import java.util.Collections;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CompleteClarifyingQuestionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.COMPLETE_CLARIFY_QUESTIONS
                || callback.getEvent() == Event.NOC_REQUEST)
            && HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData());
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<Direction>>> directionsListOptional = asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS);

        Optional<IdValue<Direction>> clarifyingQuestionsDirectionOptional = directionsListOptional.orElse(Collections.emptyList()).stream()
            .filter(directionIdVale -> directionIdVale.getValue().getTag().equals(DirectionTag.REQUEST_CLARIFYING_QUESTIONS))
            .findFirst();

        if (clarifyingQuestionsDirectionOptional.isPresent()) {
            Direction direction = clarifyingQuestionsDirectionOptional.get().getValue();
            List<IdValue<ClarifyingQuestion>> questions = direction.getClarifyingQuestions();

            Optional<List<IdValue<ClarifyingQuestionAnswer>>> existingAnswers = asylumCase.read(CLARIFYING_QUESTIONS_ANSWERS);

            // set default answer for unanswered clarifying questions
            List<IdValue<ClarifyingQuestionAnswer>> answers = questions.stream()
                    .filter(question -> isClarifyingQuestionNotAnswered(existingAnswers.orElse(emptyList()), question.getId()))
                    .map(question -> mapDefaultAnswer(question, direction, callback.getEvent()))
                    .collect(Collectors.toList());

            existingAnswers.ifPresent(answers::addAll);

            asylumCase.write(AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS, answers);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isClarifyingQuestionNotAnswered(
            List<IdValue<ClarifyingQuestionAnswer>> clarifyingQuestionsAnswers, String questionId) {

        if (clarifyingQuestionsAnswers.isEmpty()) {
            return true;
        }

        Optional<IdValue<ClarifyingQuestionAnswer>> clarifyingQuestionAnswer = clarifyingQuestionsAnswers.stream()
                .filter(questionAnswerIdValue -> questionAnswerIdValue.getId().equals(questionId))
                .findFirst();

        return clarifyingQuestionAnswer.isPresent() && isEmpty(clarifyingQuestionAnswer.get().getValue().getAnswer());
    }

    private IdValue<ClarifyingQuestionAnswer> mapDefaultAnswer(IdValue<ClarifyingQuestion> clarifyingQuestion, Direction direction, Event event) {
        ClarifyingQuestionAnswer answer = new ClarifyingQuestionAnswer(direction.getDateSent(), direction.getDateDue(),
            null, clarifyingQuestion.getValue().getQuestion(),
                event == Event.NOC_REQUEST
                    ? "No answer submitted because the question was marked as complete due to change in representation"
                    : "No answer submitted because the question was marked as complete by the Tribunal",
            direction.getUniqueId(), Collections.emptyList());
        return new IdValue<>(clarifyingQuestion.getId(), answer);
    }

}
