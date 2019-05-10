package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.EDITABLE_DIRECTIONS;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.EditableDirection;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueDateHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<Direction>>> maybeDirections = caseDataMap.get(DIRECTIONS);

        final Map<String, Direction> existingDirectionsById =
            maybeDirections
                .orElseThrow(() -> new IllegalStateException("directions is not present"))
                .stream()
                .collect(toMap(
                    IdValue::getId,
                    IdValue::getValue
                ));

        Optional<List<IdValue<EditableDirection>>> maybeEditableDirections =
                caseDataMap.get(EDITABLE_DIRECTIONS);

        List<IdValue<Direction>> changedDirections =
            maybeEditableDirections
                .orElse(emptyList())
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
                .collect(toList());

        caseDataMap.clear(EDITABLE_DIRECTIONS);
        caseDataMap.write(DIRECTIONS, changedDirections);

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
