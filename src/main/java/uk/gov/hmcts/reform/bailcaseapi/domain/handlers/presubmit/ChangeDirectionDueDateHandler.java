package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousDates;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;

@Component
public class ChangeDirectionDueDateHandler implements PreSubmitCallbackHandler<BailCase> {

    private static final String DIRECTION = "Direction ";
    private final DateProvider dateProvider;

    public ChangeDirectionDueDateHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final Optional<List<IdValue<Direction>>> maybeDirections = bailCase.read(DIRECTIONS);

        Optional<DynamicList> dynamicList =
            bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_LIST,DynamicList.class);

        DynamicList bailDirectionList = bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("bailDirectionList is missing"));

        // find selected direction's id
        Value selectedDirection = bailDirectionList.getValue();
        int selectedDirectionId = Integer.parseInt(selectedDirection.getCode().substring(DIRECTION.length()));

        List<IdValue<Direction>> directions = maybeDirections.orElse(emptyList());
        IdValue<Direction> directionBeingChanged = directions.get(selectedDirectionId - 1);

        if (dynamicList.isPresent()) {

            List<IdValue<Direction>> changedDirections =
                directions
                    .stream()
                    .map(idValue -> {

                        if (idValue.equals(directionBeingChanged)) {

                            return new IdValue<>(
                                idValue.getId(),
                                new Direction(
                                    idValue.getValue().getSendDirectionDescription(),
                                    idValue.getValue().getSendDirectionList(),
                                    bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE,
                                                  String.class).orElse(""),
                                    dateProvider.now().toString(),
                                    idValue.getValue().getDateTimeDirectionCreated(),
                                    idValue.getValue().getDateTimeDirectionModified(),
                                    appendPreviousDates(idValue.getValue().getPreviousDates(),
                                                        idValue.getValue().getDateOfCompliance(),
                                                        idValue.getValue().getDateSent())
                                )
                            );
                        } else {
                            return idValue;
                        }
                    })
                    .collect(toList());

            bailCase.clear(BAIL_DIRECTION_LIST);
            bailCase.write(DIRECTIONS, changedDirections);

        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private List<IdValue<PreviousDates>> appendPreviousDates(List<IdValue<PreviousDates>> previousDates,
                                                             String dateDue, String dateSent) {

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
