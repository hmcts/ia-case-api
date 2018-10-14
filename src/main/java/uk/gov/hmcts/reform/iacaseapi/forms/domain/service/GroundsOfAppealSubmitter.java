package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseEventProcessor;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.Event;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.EventWithCaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

@Service
public class GroundsOfAppealSubmitter {

    private final CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor;

    public GroundsOfAppealSubmitter(
        @Autowired CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor
    ) {
        this.ccdAsylumCaseEventProcessor = ccdAsylumCaseEventProcessor;
    }

    public void submit(
        final String caseId,
        List<String> groundsOfAppeal
    ) {
        EventWithCaseData<AsylumCase> eventWithCaseData =
            ccdAsylumCaseEventProcessor.startEvent(
                caseId,
                new Event(EventId.EDIT_GROUNDS_OF_APPEAL)
            );

        eventWithCaseData
            .getCaseData()
            .orElseThrow(() -> new IllegalStateException("caseData not present"))
            .setAppealGrounds(groundsOfAppeal);

        ccdAsylumCaseEventProcessor.completeEvent(
            caseId,
            eventWithCaseData
        );
    }
}
