package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewHearingRequirementsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final YesOrNo reviewedHearingRequirements =
            asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)
            .orElseThrow(() -> new IllegalStateException("reviewHearingRequirements flag should be present"));

        if (callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS && reviewedHearingRequirements.equals(YesOrNo.YES)) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. The hearing requirements have already been reviewed.");
            return asylumCasePreSubmitCallbackResponse;
        }

        final Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);
        final Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguage = asylumCase.read(INTERPRETER_LANGUAGE);

        if (witnessDetails.isPresent()) {
            asylumCase.write(WITNESS_DETAILS_READONLY, witnessDetails
                .get()
                .stream()
                .map(w ->
                    "Name\t\t" + w.getValue().getWitnessName())
                .collect(Collectors.joining("\n")));
        }

        if (interpreterLanguage.isPresent()) {
            asylumCase.write(INTERPRETER_LANGUAGE_READONLY, interpreterLanguage
                .get()
                .stream()
                .map(i ->
                    "Language\t\t" + i.getValue().getLanguage() + "\nDialect\t\t\t" + i.getValue().getLanguageDialect() + "\n")
                .collect(Collectors.joining("\n")));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
