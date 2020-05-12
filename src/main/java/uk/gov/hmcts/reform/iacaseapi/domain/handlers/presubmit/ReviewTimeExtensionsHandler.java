package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DateAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionFinder;

@Component
public class ReviewTimeExtensionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DirectionFinder directionFinder;

    public ReviewTimeExtensionsHandler(DirectionFinder directionFinder) {
        this.directionFinder = directionFinder;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REVIEW_TIME_EXTENSION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        State currentState = callback.getCaseDetails().getState();

        Optional<IdValue<Direction>> directionBeingUpdated = directionFinder.getUpdatableDirectionForState(currentState, asylumCase);
        if (!directionBeingUpdated.isPresent()) {
            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("No extendable direction for current state");
            return asylumCasePreSubmitCallbackResponse;
        }

        String decisionOutcomeDueDate = getTimeExtensionDueDate(asylumCase);
        String currentDueDate = getDirectionDueDate(directionBeingUpdated);
        String updatedDecisionDueDate = decisionOutcomeDueDate == null
            ? currentDueDate
            : decisionOutcomeDueDate;

        LocalDate currentDueDateFormatted = LocalDate.parse(currentDueDate);
        LocalDate updatedDecisionDueDateFormatted = LocalDate.parse(updatedDecisionDueDate);

        if (!updatedDecisionDueDateFormatted.isEqual(currentDueDateFormatted) && !updatedDecisionDueDateFormatted.isAfter(currentDueDateFormatted)) {
            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The new direction due date must be after the previous direction due date");
            return asylumCasePreSubmitCallbackResponse;
        }

        List<IdValue<TimeExtension>> timeExtensions = getTimeExtensions(asylumCase).map(timeExtension -> {
            TimeExtension timeExtensionValue = timeExtension.getValue();
            if (currentState == timeExtensionValue.getState() && timeExtensionValue.getStatus() == SUBMITTED) {

                TimeExtensionDecision timeExtensionDecision = getTimeExtensionDecision(asylumCase);
                TimeExtensionStatus timeExtensionStatus = timeExtensionDecision == TimeExtensionDecision.REFUSED ? REFUSED : GRANTED;
                return new IdValue<>(timeExtension.getId(), new TimeExtension(
                    timeExtensionValue.getRequestDate(),
                    timeExtensionValue.getReason(),
                    timeExtensionValue.getState(),
                    timeExtensionStatus,
                    timeExtensionValue.getEvidence(),
                    timeExtensionDecision,
                    getTimeExtensionDecisionReason(asylumCase),
                    updatedDecisionDueDate
                ));
            }
            return timeExtension;
        }).collect(Collectors.toList());

        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);
        List<IdValue<Direction>> changedDirections =
            maybeDirections.orElse(emptyList())
                .stream()
                .map(idValue -> {
                    if (directionBeingUpdated.isPresent() && directionBeingUpdated.get().getId().equals(idValue.getId())) {
                        return new IdValue<>(
                            idValue.getId(),
                            new Direction(
                                idValue.getValue().getExplanation(),
                                idValue.getValue().getParties(),
                                updatedDecisionDueDate,
                                idValue.getValue().getDateSent(),
                                idValue.getValue().getTag(),
                                DateAppender.appendPreviousDates(idValue.getValue().getPreviousDates(), idValue.getValue().getDateDue(), idValue.getValue().getDateSent())
                            )
                        );
                    } else {
                        return idValue;
                    }
                })
                .collect(toList());

        asylumCase.write(TIME_EXTENSIONS, timeExtensions);
        asylumCase.write(DIRECTIONS, changedDirections);
        asylumCase.write(REVIEW_TIME_EXTENSION_REQUIRED, YesOrNo.NO);


        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private TimeExtensionDecision getTimeExtensionDecision(AsylumCase asylumCase) {
        Optional<TimeExtensionDecision> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision"));
    }

    private String getTimeExtensionDecisionReason(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION_REASON);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision reason"));
    }

    private String getDirectionDueDate(Optional<IdValue<Direction>> direction) {
        if (!direction.isPresent()) {
            throw new IllegalArgumentException("Could not find direction due date");
        }
        return direction.get().getValue().getDateDue();
    }

    private String getTimeExtensionDueDate(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(REVIEW_TIME_EXTENSION_DUE_DATE);
        return read.orElse(null);
    }


    private Stream<IdValue<TimeExtension>> getTimeExtensions(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<TimeExtension>>>read(TIME_EXTENSIONS)
            .orElse(emptyList()).stream();
    }
}
