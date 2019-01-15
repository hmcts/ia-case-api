package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.EditableDirection;
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

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<IdValue<EditableDirection>> editableDirections =
            asylumCase
                .getDirections()
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

        asylumCase.setEditableDirections(editableDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
