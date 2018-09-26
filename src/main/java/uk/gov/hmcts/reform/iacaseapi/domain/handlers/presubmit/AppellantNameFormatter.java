package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Name;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.DispatchPriority;

@Component
public class AppellantNameFormatter implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.START_APPEAL
                   || ccdEvent.getEventId() == EventId.CHANGE_APPEAL);
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
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

        Name appellantName =
            asylumCase
                .getAppellantName()
                .orElseThrow(() -> new IllegalStateException("appellantName is not present"));

        String appellantNameForDisplay =
            appellantName.getTitle().orElse("")
            + " "
            + appellantName.getFirstName().orElse("")
            + " "
            + appellantName.getLastName().orElse("");

        asylumCase.setAppellantNameForDisplay(
            appellantNameForDisplay.replaceAll("\\s+", " ").trim()
        );

        return preSubmitResponse;
    }
}
