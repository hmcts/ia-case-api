package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Service
@RequiredArgsConstructor
public class UpdateInterpreterDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    /**
     * Add a unique id to each interpreter if it doesn't already have one.
     */
    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<InterpreterDetails>>> optionalInterpreterDetailsList = asylumCase
            .read(INTERPRETER_DETAILS);

        List<IdValue<InterpreterDetails>> interpreterDetailsList =
            generateInterpreterDetailsWithId(optionalInterpreterDetailsList);

        if (!interpreterDetailsList.isEmpty()) {
            asylumCase.write(INTERPRETER_DETAILS, interpreterDetailsList);
        }

        return new PreSubmitCallbackResponse<>(iaHearingsApiService.updateHearing(callback));

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
                }).toList())
            .orElse(Collections.emptyList());
    }
}
