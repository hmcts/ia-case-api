package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueMidEvent implements PreSubmitCallbackHandler<BailCase> {

    private static final String DIRECTION = "Direction ";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<Direction>>> maybeDirections = bailCase.read(DIRECTIONS);

        DynamicList bailDirectionList = bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("bailDirectionList is missing"));

        // find selected direction's id
        Value selectedDirection = bailDirectionList.getValue();
        int selectedDirectionId = Integer.parseInt(selectedDirection.getCode().substring(DIRECTION.length()));

        List<IdValue<Direction>> directions = maybeDirections.orElse(emptyList());
        if (!directions.isEmpty()) {
            // find direction to be changed
            // e.g. what is Direction 4 on UI has index 3 in the collection
            IdValue<Direction> directionToChange = directions.get(selectedDirectionId - 1);

            // write fields of the direction that's being changed into the bailcase object if present
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_EXPLANATION,
                           directionToChange.getValue().getSendDirectionDescription());
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES,
                           directionToChange.getValue().getSendDirectionList());
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE,
                           directionToChange.getValue().getDateOfCompliance());
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_SENT,
                           directionToChange.getValue().getDateSent());
        } else {
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_EXPLANATION, "");
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES, "");
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE, "");
            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_SENT, "");
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
