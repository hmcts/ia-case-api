package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

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
public class RemoveFlagPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public RemoveFlagPreparer() {

    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.REMOVE_FLAG;
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

        final Optional<List<IdValue<LegacyCaseFlag>>> maybeExistingCaseFlags = asylumCase.read(LEGACY_CASE_FLAGS);

        final List<Value> existingCaseFlagListElements =
            getExistingCaseFlagListElements(maybeExistingCaseFlags.orElse(Collections.emptyList()));

        if (existingCaseFlagListElements.isEmpty()) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("There are no flags in this case");
            return response;
        }

        DynamicList caseFlagTypes = createDynamicList(existingCaseFlagListElements);

        asylumCase.write(REMOVE_FLAG_TYPE_OF_FLAG, caseFlagTypes);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public List<Value> getExistingCaseFlagListElements(List<IdValue<LegacyCaseFlag>> existingCaseFlags) {
        requireNonNull(existingCaseFlags, "existingCaseFlags must not be null");
        return existingCaseFlags
            .stream()
            .map(idValue -> new Value(idValue.getId(), idValue.getValue().getLegacyCaseFlagType().getReadableText()))
            .collect(Collectors.toList());
    }

    public DynamicList createDynamicList(List<Value> elementsList) {
        requireNonNull(elementsList, "elementsList must not be null");
        return new DynamicList(elementsList.get(0), elementsList);
    }
}
