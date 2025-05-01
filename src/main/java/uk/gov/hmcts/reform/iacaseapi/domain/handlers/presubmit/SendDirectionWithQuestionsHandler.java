package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
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

@Slf4j
@Component
public class SendDirectionWithQuestionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DirectionAppender appender;

    @Autowired
    public SendDirectionWithQuestionsHandler(DateProvider dateProvider, DirectionAppender appender) {
        this.dateProvider = dateProvider;
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
        boolean directionDueDateIsInFuture = LocalDate.parse(directionDueDate).isAfter(dateProvider.now());

        if (!directionDueDateIsInFuture) {

            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

            log.error("Direction due date must be in the future");
            response.addError("Direction due date must be in the future");
            return response;
        }

        log.info("Direction due date is in the future : {}", directionDueDate);
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

        for (IdValue<Direction> idValue : allDirections) {
            log.info("Direction: {}", idValue.getValue().toString());
        }

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
