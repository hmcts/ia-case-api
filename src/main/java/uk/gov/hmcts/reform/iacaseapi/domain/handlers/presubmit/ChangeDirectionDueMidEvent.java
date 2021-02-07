package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
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
public class ChangeDirectionDueMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String direction = "Direction ";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
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

        DynamicList directionList = asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("directionList is missing"));

        maybeDirections
            .orElse(emptyList())
            .stream()
            .filter(idValue -> directionList.getValue().getCode().contains(direction + (maybeDirections.orElse(emptyList()).size() - (Integer.parseInt(idValue.getId())) + 1)))
            .forEach(idValue -> {

                asylumCase.write(AsylumCaseFieldDefinition.DIRECTION_EDIT_EXPLANATION, idValue.getValue().getExplanation());
                asylumCase.write(AsylumCaseFieldDefinition.DIRECTION_EDIT_PARTIES, idValue.getValue().getParties().toString());
                asylumCase.write(AsylumCaseFieldDefinition.DIRECTION_EDIT_DATE_DUE, idValue.getValue().getDateDue());
                asylumCase.write(AsylumCaseFieldDefinition.DIRECTION_EDIT_DATE_SENT, idValue.getValue().getDateSent());
            });

        List<Value> directionListElements = maybeDirections
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(direction + idValue.getId(), direction + idValue.getId()))
            .collect(Collectors.toList());

        Collections.reverse(directionListElements);
        DynamicList newDirectionList = new DynamicList(new Value(directionList.getValue().getCode(), directionList.getValue().getCode()), directionListElements);
        asylumCase.write(DIRECTION_LIST, newDirectionList);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
