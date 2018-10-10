package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;

@Component
public class DateOfBirthValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private final static int MINIMUM_ALLOWED_AGE_IN_YEARS = 18;

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEventId() == EventId.START_APPEAL
                   || callback.getEventId() == EventId.CHANGE_APPEAL);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        if (asylumCase.getAppellantDob().isPresent()) {

            LocalDate appellantDob =
                LocalDate.parse(
                    asylumCase.getAppellantDob().get(),
                    DateTimeFormatter.ISO_LOCAL_DATE
                );

            int age = Period.between(appellantDob, LocalDate.now()).getYears();
            if (age < MINIMUM_ALLOWED_AGE_IN_YEARS) {
                preSubmitResponse
                    .getErrors()
                    .add("Appellant is too young to use this service");
            }
        }

        return preSubmitResponse;
    }
}
