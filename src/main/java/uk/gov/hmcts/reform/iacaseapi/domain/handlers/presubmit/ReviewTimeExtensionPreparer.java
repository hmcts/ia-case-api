package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.SUBMITTED;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewTimeExtensionPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REVIEW_TIME_EXTENSION;
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


        State currentState = callback.getCaseDetails().getState();
        return getTimeExtensions(asylumCase)
            .filter(submittedTimeExtensionForCurrentState(currentState))
            .findFirst()
            .map(setReviewTimeExtensionValues(asylumCase))
            .orElseGet(errorIfNoTimeExtensionRequested(asylumCase));
    }

    private Stream<IdValue<TimeExtension>> getTimeExtensions(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<TimeExtension>>>read(TIME_EXTENSIONS)
            .orElse(Collections.emptyList()).stream();
    }

    private Predicate<IdValue<TimeExtension>> submittedTimeExtensionForCurrentState(State currentState) {
        return timeExtension -> currentState == timeExtension.getValue().getState() && timeExtension.getValue().getStatus() == SUBMITTED;
    }

    private Function<IdValue<TimeExtension>, PreSubmitCallbackResponse<AsylumCase>> setReviewTimeExtensionValues(AsylumCase asylumCase) {
        Optional<List<IdValue<Direction>>> directions = asylumCase.read(DIRECTIONS);
        Optional<IdValue<Direction>> directionBeingUpdated = directions.orElse(emptyList()).stream()
            .filter(directionIdVale -> directionIdVale.getValue().getTag().equals(DirectionTag.REQUEST_REASONS_FOR_APPEAL)
                //todo this needs to work for a number of states
            ).findFirst();

        return timeExtensionIdValue -> {
            asylumCase.write(REVIEW_TIME_EXTENSION_DATE, timeExtensionIdValue.getValue().getRequestDate());
            asylumCase.write(REVIEW_TIME_EXTENSION_PARTY, Parties.APPELLANT);
            asylumCase.write(REVIEW_TIME_EXTENSION_REASON, timeExtensionIdValue.getValue().getReason());
            asylumCase.write(REVIEW_TIME_EXTENSION_DECISION, null);
            asylumCase.write(REVIEW_TIME_EXTENSION_DECISION_REASON, "");
            IdValue<Direction> directionIdValue = directionBeingUpdated.get();
            asylumCase.write(REVIEW_TIME_EXTENSION_DUE_DATE, directionIdValue.getValue().getDateDue());

            return new PreSubmitCallbackResponse<>(asylumCase);
        };
    }

    private Supplier<PreSubmitCallbackResponse<AsylumCase>> errorIfNoTimeExtensionRequested(AsylumCase asylumCase) {
        return () -> {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("There is no time extension to review");
            return response;
        };
    }
}
