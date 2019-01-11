package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueDateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final Map<String, Direction> existingDirectionsById =
            asylumCase
                .getDirections()
                .orElseThrow(() -> new IllegalStateException("directions is not present"))
                .stream()
                .collect(Collectors.toMap(
                    IdValue::getId,
                    IdValue::getValue
                ));

        List<IdValue<Direction>> changedDirections =
            asylumCase
                .getEditableDirections()
                .orElse(Collections.emptyList())
                .stream()
                .map(idValue -> {

                    Direction existingDirection =
                        existingDirectionsById
                            .get(idValue.getId());

                    if (existingDirection == null) {
                        throw new IllegalStateException("Cannot find original direction to update");
                    }

                    return new IdValue<>(
                        idValue.getId(),
                        new Direction(
                            existingDirection.getExplanation(),
                            existingDirection.getParties(),
                            idValue.getValue().getDateDue(),
                            existingDirection.getDateSent(),
                            existingDirection.getTag()
                        )
                    );

                })
                .collect(Collectors.toList());

        asylumCase.clearEditableDirections();
        asylumCase.setDirections(changedDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
