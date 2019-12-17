package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueDatePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE;
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

        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);

        List<Value> directionListElements = maybeDirections
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value("Direction " + idValue.getId(), "Direction " + idValue.getId()))
            .collect(Collectors.toList());

        if (directionListElements.isEmpty()) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("There is no direction to edit");
            return response;
        }

        Collections.reverse(directionListElements);
        DynamicList directionList = new DynamicList(directionListElements.get(0), directionListElements);
        // remove for now RIA-723
        //asylumCase.write(DIRECTION_LIST, directionList);

        List<IdValue<EditableDirection>> editableDirections =
            maybeDirections
                .orElse(Collections.emptyList())
                .stream()
                .map(idValue ->
                    new IdValue<>(
                        idValue.getId(),
                        new EditableDirection(
                            idValue.getValue().getExplanation(),
                            idValue.getValue().getParties(),
                            idValue.getValue().getDateDue()
                        )
                    )
                )
                .collect(Collectors.toList());
        asylumCase.write(EDITABLE_DIRECTIONS, editableDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
