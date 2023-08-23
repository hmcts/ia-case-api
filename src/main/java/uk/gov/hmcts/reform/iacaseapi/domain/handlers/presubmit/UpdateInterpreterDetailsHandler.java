package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Service
public class UpdateInterpreterDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    /**
     * Add a unique id to each interpreter if it doesn't already have one.
     */
    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<InterpreterDetails>>> optionalInterpreterDetailsList = asylumCase.read(INTERPRETER_DETAILS);

        if (optionalInterpreterDetailsList.isPresent()) {
            asylumCase.write(INTERPRETER_DETAILS, generateInterpreterDetailsWithId(optionalInterpreterDetailsList));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static List<IdValue<InterpreterDetails>> generateInterpreterDetailsWithId(
        Optional<List<IdValue<InterpreterDetails>>> interpreterDetailsList) {
        interpreterDetailsList.get().stream().map(IdValue::getValue).forEach(details -> {
            if (details.getInterpreterId() == null) {
                details.setInterpreterId(UUID.randomUUID().toString());
            }
        });

        return interpreterDetailsList.get();
    }
}
