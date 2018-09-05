package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Issues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class UpdateSummaryUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(ServeDirectionUpdater.class);

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.UPDATE_SUMMARY;
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

        Issues issues =
            asylumCase
                .getIssues()
                .orElseThrow(() -> new IllegalStateException("issues not present"));

        asylumCase
            .getCaseSummary()
            .orElseThrow(() -> new IllegalStateException("caseSummary not present"))
            .setIssues(issues);

        return preSubmitResponse;
    }
}
