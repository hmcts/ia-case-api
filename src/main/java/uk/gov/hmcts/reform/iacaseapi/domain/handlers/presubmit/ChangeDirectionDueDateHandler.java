package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EDITABLE_DIRECTIONS;

import java.util.*;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueDateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public ChangeDirectionDueDateHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

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

        Optional<DynamicList> dynamicList = asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_LIST, DynamicList.class);

        // new path when dynamic list is present
        if (dynamicList.isPresent()) {

            // commented to prioritise release queue
            //List<IdValue<Direction>> changedDirections =
            //    maybeDirections.orElse(emptyList())
            //        .stream()
            //        .map(idValue -> {
            //
            //            if (dynamicList.get().getValue().getCode().contains("Direction " + (maybeDirections.orElse(emptyList()).size() - (Integer.parseInt(idValue.getId())) + 1))) {
            //                return new IdValue<>(
            //                    idValue.getId(),
            //                    new Direction(
            //                        idValue.getValue().getExplanation(),
            //                        idValue.getValue().getParties(),
            //                        asylumCase.read(AsylumCaseFieldDefinition.DIRECTION_EDIT_DATE_DUE, String.class).orElse(""),
            //                        dateProvider.now().toString(),
            //                        idValue.getValue().getTag(),
            //                        appendPreviousDates(idValue.getValue().getPreviousDates(), idValue.getValue().getDateDue(), idValue.getValue().getDateSent())
            //                    )
            //                );
            //            } else {
            //                return idValue;
            //            }
            //        })
            //        .collect(toList());
            //
            //asylumCase.clear(DIRECTION_LIST);
            //
            //asylumCase.write(DIRECTIONS, changedDirections);

        } /* compatibility with old CCD definitions (remove on next release) */ else {

            Map<String, Direction> existingDirectionsById =
                maybeDirections
                    .orElseThrow(() -> new IllegalStateException("directions is not present"))
                    .stream()
                    .collect(toMap(
                        IdValue::getId,
                        IdValue::getValue
                    ));

            Optional<List<IdValue<EditableDirection>>> maybeEditableDirections =
                asylumCase.read(EDITABLE_DIRECTIONS);

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

            asylumCase.clear(EDITABLE_DIRECTIONS);
            asylumCase.write(DIRECTIONS, changedDirections);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<IdValue<PreviousDates>> appendPreviousDates(List<IdValue<PreviousDates>> previousDates, String dateDue, String dateSent) {

        if (CollectionUtils.isEmpty(previousDates)) {

            return newArrayList(new IdValue<>("1", new PreviousDates(dateDue, dateSent)));
        } else {

            int index = previousDates.size() + 1;

            final List<IdValue<PreviousDates>> allPreviousDates = new ArrayList<>();
            allPreviousDates.add(new IdValue<>(String.valueOf(index--), new PreviousDates(dateDue, dateSent)));

            for (IdValue<PreviousDates> previousDate : previousDates) {
                allPreviousDates.add(new IdValue<>(String.valueOf(index--), previousDate.getValue()));
            }

            return allPreviousDates;
        }
    }
}
