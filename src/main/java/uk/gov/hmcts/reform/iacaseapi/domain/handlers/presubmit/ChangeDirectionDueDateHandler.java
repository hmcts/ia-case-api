package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EDITABLE_DIRECTIONS;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
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

        final Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);
        final DynamicList directionList = asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_LIST, DynamicList.class).orElse(null);

        List<IdValue<Direction>> changedDirections =
            maybeDirections.orElse(emptyList())
                .stream()
                .map(idValue -> {

                    if(directionList.getValue().getCode().contains("Direction " + (maybeDirections.orElse(emptyList()).size() - (Integer.parseInt(idValue.getId())) + 1))) {
                        return new IdValue<>(
                            idValue.getId(),
                            new Direction(
                                asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_EDIT_EXPLANATION, String.class).orElse(""),
                                asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_EDIT_PARTIES, Parties.class).orElseThrow(() -> new IllegalStateException("")),
                                asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_EDIT_DATE_DUE, String.class).orElse(""),
                                LocalDate.now().toString(),
                                idValue.getValue().getTag()
                            )
                        );
                    } else {
                        return idValue;
                    }
                })
                .collect(toList());

        asylumCase.clear(DIRECTION_LIST);

        asylumCase.write(DIRECTIONS, changedDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
