package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewCmaRequirementsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REVIEW_CMA_REQUIREMENTS;
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

        decorateInterpreterDetails(asylumCase);
        decorateDatesToAvoid(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static void decorateInterpreterDetails(AsylumCase asylumCase) {
        final Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguage = asylumCase.read(INTERPRETER_LANGUAGE);

        interpreterLanguage.ifPresent(idValues -> asylumCase.write(INTERPRETER_LANGUAGE_READONLY, idValues
            .stream()
            .map(i ->
                "Language\t\t" + i.getValue().getLanguage() + "\nDialect\t\t\t" + i.getValue().getLanguageDialect() + "\n")
            .collect(Collectors.joining("\n"))));
    }

    private static void decorateDatesToAvoid(AsylumCase asylumCase) {
        final Optional<List<IdValue<DatesToAvoid>>> datesToAvoid = asylumCase.read(DATES_TO_AVOID);

        datesToAvoid.ifPresent(idValues -> asylumCase.write(DATES_TO_AVOID_READONLY, idValues
            .stream()
            .map(i ->
                "Date\t\t\t" + i.getValue().getDateToAvoid() + "\nReason\t\t\t" + i.getValue().getDateToAvoidReason() + "\n")
            .collect(Collectors.joining("\n"))));
    }
}
