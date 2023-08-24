package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import org.apache.commons.validator.routines.EmailValidator;

@Component
public class UpdateInterpreterDetailsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String INVALID_EMAIL_ERROR_MESSAGE = "Interpreter %s email address is invalid, please enter a valid email address.";

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    /**
     * This handler is used to validate the interpreter details email address entered by the user.
     */
    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        Optional<List<IdValue<InterpreterDetails>>> optionalInterpreterDetailsList = asylumCase.read(INTERPRETER_DETAILS);

        if (optionalInterpreterDetailsList.isPresent()) {
            validateInterpretersEmailAddresses(optionalInterpreterDetailsList, response);
        }

        return response;
    }

    private static void validateInterpretersEmailAddresses(Optional<List<IdValue<InterpreterDetails>>> optionalInterpreterDetailsList,
                                                           PreSubmitCallbackResponse<AsylumCase> response) {
        EmailValidator validator = EmailValidator.getInstance();
        AtomicInteger counter = new AtomicInteger(0);

        optionalInterpreterDetailsList.get().stream().map(IdValue::getValue).forEach(details -> {
            counter.incrementAndGet();
            if (!validator.isValid(details.getInterpreterEmail())) {
                response.addError(String.format(INVALID_EMAIL_ERROR_MESSAGE, counter.get()));
            }
        });
    }
}
