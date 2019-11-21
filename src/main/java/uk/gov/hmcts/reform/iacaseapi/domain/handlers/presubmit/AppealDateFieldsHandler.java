package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Service
public class AppealDateFieldsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public AppealDateFieldsHandler(
        DateProvider dateProvider) {

        this.dateProvider = dateProvider;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && Arrays.asList(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        Optional<String> maybeHomeOfficeDecisionDate = asylumCase.read(HOME_OFFICE_DECISION_DATE);

        LocalDate homeOfficeDecisionDate =
            parse(maybeHomeOfficeDecisionDate
                .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is not present")));

        if (homeOfficeDecisionDate.isAfter(dateProvider.now())) {
            callbackResponse.addError("You've entered an invalid date. You cannot enter a date in the future.");
            return callbackResponse;
        }

        Optional<String> maybeAppellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH);

        if (maybeAppellantDateOfBirth.isPresent()) {

            LocalDate appellantDateOfBirth = parse(maybeAppellantDateOfBirth.get());

            if (appellantDateOfBirth.isAfter(dateProvider.now())) {
                callbackResponse.addError("You've entered an invalid date. You cannot enter a date in the future.");
                return callbackResponse;
            }
        }

        return callbackResponse;
    }
}
