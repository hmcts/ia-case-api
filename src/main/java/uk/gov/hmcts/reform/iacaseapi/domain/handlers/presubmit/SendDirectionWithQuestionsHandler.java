package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED;

@Component
public class SendDirectionWithQuestionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DirectionAppender appender;

    @Autowired
    public SendDirectionWithQuestionsHandler(DirectionAppender appender) {
        this.appender = appender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SEND_DIRECTION_WITH_QUESTIONS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<String> directionDueDateOptional = asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE, String.class);

        String directionDueDate = directionDueDateOptional.orElse("");

        Optional<List<IdValue<Direction>>> directionsOptional = asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS);
        List<IdValue<Direction>> directions = directionsOptional.orElse(Collections.emptyList());

        Optional<List<IdValue<ClarifyingQuestion>>> questions = asylumCase.read(AsylumCaseFieldDefinition.SEND_DIRECTION_QUESTIONS);

        List<IdValue<Direction>> allDirections = appender.append(
                asylumCase,
                directions,
                "You need to answer some questions about your appeal.",
                Parties.APPELLANT,
                directionDueDate,
                DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
                questions.orElse(Collections.emptyList()),
                callback.getEvent().toString()
        );
        asylumCase.write(DIRECTIONS, allDirections);

        Optional<CaseDetails<AsylumCase>> beforeCaseDetails = callback.getCaseDetailsBefore();
        if (beforeCaseDetails.isPresent()) {
            State preClarifyingState = beforeCaseDetails.get().getState();
            if (preClarifyingState != AWAITING_CLARIFYING_QUESTIONS_ANSWERS
                && preClarifyingState != CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED) {
                asylumCase.write(AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE, preClarifyingState);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
