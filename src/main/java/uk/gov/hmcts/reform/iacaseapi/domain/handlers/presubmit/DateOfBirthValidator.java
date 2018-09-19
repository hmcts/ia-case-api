package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class DateOfBirthValidator implements CcdEventPreSubmitHandler<AsylumCase> {

    private final static int MINIMUM_ALLOWED_AGE_IN_YEARS = 18;

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.START_APPEAL
                   || ccdEvent.getEventId() == EventId.CHANGE_APPEAL);
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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
