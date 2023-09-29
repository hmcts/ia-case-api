package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LEVEL_FLAGS;

@Slf4j
@Component
public class RemoveCaseFlagHandler implements PreSubmitCallbackHandler<AsylumCase> {


    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.UPDATE_HEARING_REQUIREMENTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        handleRemovalOfInterpreterLevelFlags(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void handleRemovalOfInterpreterLevelFlags(AsylumCase asylumCase) {
        Optional<List<PartyFlagIdValue>> interpreterLevelFlagsOptional = asylumCase.read(INTERPRETER_LEVEL_FLAGS);
        Optional<List<IdValue<InterpreterDetails>>> interpreterDetailsOptional = asylumCase.read(INTERPRETER_DETAILS);
        Optional<List<PartyFlagIdValue>> finalPartyFlagList = Optional.empty();

        if (interpreterLevelFlagsOptional.isPresent() && interpreterDetailsOptional.isPresent()) {
            List<String> interpreterIds = new ArrayList<>();
            interpreterDetailsOptional.get().forEach(detail -> interpreterIds.add(detail.getValue().getInterpreterId()));

            finalPartyFlagList = Optional.of(interpreterLevelFlagsOptional.get()
                    .stream()
                    .filter(id -> interpreterIds.contains(id.getPartyId()))
                    .collect(Collectors.toList()));
        }
        asylumCase.write(INTERPRETER_LEVEL_FLAGS, finalPartyFlagList);
    }
}
