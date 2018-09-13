package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseArgument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class UpdateSummaryPreparer implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(UpdateSummaryPreparer.class);

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_START
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

        asylumCase.clearGroundsForAppeal();
        asylumCase.clearIssues();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

        CaseArgument caseArgument =
            asylumCase
                .getCaseArgument()
                .orElseThrow(() -> new IllegalStateException("caseArgument not present"));

        if (caseArgument
            .getGroundsForAppeal()
            .isPresent()) {

            asylumCase.setGroundsForAppeal(
                caseArgument
                    .getGroundsForAppeal()
                    .get()
            );
        }

        if (caseArgument
            .getIssues()
            .isPresent()) {

            asylumCase.setIssues(
                caseArgument
                    .getIssues()
                    .get()
            );
        }

        return preSubmitResponse;
    }
}
