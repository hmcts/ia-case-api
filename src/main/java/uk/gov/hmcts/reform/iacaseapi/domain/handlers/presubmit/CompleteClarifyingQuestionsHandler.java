package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
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
            && callback.getEvent() == Event.COMPLETE_CLARIFY_QUESTIONS
            && isAipJourney(callback);
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

            List<IdValue<ClarifyingQuestionAnswer>> answers = questions
                .stream().map(x -> mapDefaultAnswer(x, direction))
                .collect(Collectors.toList());

            Optional<List<IdValue<ClarifyingQuestionAnswer>>> existingAnswers = asylumCase.read(CLARIFYING_QUESTIONS_ANSWERS);
            existingAnswers.ifPresent(answers::addAll);

            asylumCase.write(AsylumCaseFieldDefinition.CLARIFYING_QUESTIONS_ANSWERS, answers);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private IdValue<ClarifyingQuestionAnswer> mapDefaultAnswer(IdValue<ClarifyingQuestion> clarifyingQuestion, Direction direction) {
        ClarifyingQuestionAnswer answer = new ClarifyingQuestionAnswer(direction.getDateSent(), direction.getDateDue(),
            null, clarifyingQuestion.getValue().getQuestion(),
            "No answer submitted because the question was marked as complete by the Tribunal", Collections.emptyList());
        return new IdValue<>(clarifyingQuestion.getId(), answer);
    }

    private boolean isAipJourney(Callback<AsylumCase> callback) {
        Optional<JourneyType> journeyTypeOptional = callback.getCaseDetails().getCaseData().read(JOURNEY_TYPE);
        return journeyTypeOptional.map(journeyType -> journeyType == JourneyType.AIP).orElse(false);
    }

}
