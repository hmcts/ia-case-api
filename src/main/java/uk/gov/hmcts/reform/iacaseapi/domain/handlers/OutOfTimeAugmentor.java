package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.CcdEventHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

@Component
public class OutOfTimeAugmentor implements CcdEventHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.START_DRAFT_APPEAL
                   || ccdEvent.getEventId() == EventId.COMPLETE_DRAFT_APPEAL
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

        if (Strings.isNotBlank(asylumCase.getHomeOfficeDecisionDate())) {

            LocalDate decisionDate =
                LocalDate.parse(
                    asylumCase.getHomeOfficeDecisionDate(),
                    DateTimeFormatter.ISO_LOCAL_DATE
                );

            int periodInMonths = Period.between(decisionDate, LocalDate.now()).getMonths();
            if (periodInMonths >= 1) {
                asylumCase.setApplicationOutOfTime("Yes");
            } else {
                asylumCase.setApplicationOutOfTime("No");
            }
        }

        return ccdEventResponse;
    }
}
