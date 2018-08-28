package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.CcdEventHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

@Component
public class SubmittedConfirmation implements CcdEventHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && (ccdEvent.getEventId() == EventId.START_DRAFT_APPEAL
                   || ccdEvent.getEventId() == EventId.COMPLETE_DRAFT_APPEAL
                   || ccdEvent.getEventId() == EventId.UPDATE_DRAFT_APPEAL
                   || ccdEvent.getEventId() == EventId.SUBMIT_APPEAL);
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

        ccdEventResponse.setConfirmationHeader("# Confirmation Header");
        ccdEventResponse.setConfirmationBody("# Confirmation Body");

        return ccdEventResponse;
    }
}
