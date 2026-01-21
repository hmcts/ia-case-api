package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_DETAILS;

@Service
public class UpdateBailInterpreterDetailsHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    /**
     * Add a unique id to each interpreter if it doesn't already have one.
     */
    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<InterpreterDetails>>> optionalInterpreterDetailsList = bailCase
            .read(INTERPRETER_DETAILS);

        List<IdValue<InterpreterDetails>> interpreterDetailsList =
            generateInterpreterDetailsWithId(optionalInterpreterDetailsList);

        if (!interpreterDetailsList.isEmpty()) {
            bailCase.write(INTERPRETER_DETAILS, interpreterDetailsList);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private static List<IdValue<InterpreterDetails>> generateInterpreterDetailsWithId(
        Optional<List<IdValue<InterpreterDetails>>> interpreterDetailsList) {
        return interpreterDetailsList.map(detailsList ->
            detailsList.stream()
                .map(idValue -> {
                    InterpreterDetails details = idValue.getValue();
                    if (details.getInterpreterId() == null || details.getInterpreterId().isBlank()) {
                        details.setInterpreterId(UUID.randomUUID().toString());
                    }

                    return new IdValue<>(idValue.getId(), details);
                }).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }
}
