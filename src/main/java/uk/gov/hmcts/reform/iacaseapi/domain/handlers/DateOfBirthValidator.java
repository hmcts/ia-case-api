package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.CcdEventHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

@Component
public class DateOfBirthValidator implements CcdEventHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.COMPLETE_DRAFT_APPEAL
                   || ccdEvent.getEventId() == EventId.UPDATE_DRAFT_APPEAL);
    }

    public CcdEventResponse<AsylumCase> handle(
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

        CcdEventResponse<AsylumCase> ccdEventResponse =
            new CcdEventResponse<>(asylumCase);

        if (asylumCase.getAppellantDob().isPresent()) {

            LocalDate appellantDob =
                LocalDate.parse(
                    asylumCase.getAppellantDob().get(),
                    DateTimeFormatter.ISO_LOCAL_DATE
                );

            int age = Period.between(appellantDob, LocalDate.now()).getYears();
            if (age < 18) {
                ccdEventResponse
                    .getErrors()
                    .add("Appellant is too young to use this service");
            }
        }

        return ccdEventResponse;
    }
}
