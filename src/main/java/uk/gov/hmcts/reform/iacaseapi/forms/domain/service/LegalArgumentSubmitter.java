package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseEventProcessor;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.Event;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.EventWithCaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.LegalArgument;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

@Service
public class LegalArgumentSubmitter {

    private final CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor;

    public LegalArgumentSubmitter(
        @Autowired CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor
    ) {
        this.ccdAsylumCaseEventProcessor = ccdAsylumCaseEventProcessor;
    }

    public void submit(
        final String caseId,
        LegalArgument legalArgument
    ) {
        EventWithCaseData<AsylumCase> eventWithCaseData =
            ccdAsylumCaseEventProcessor.startEvent(
                caseId,
                new Event(EventId.BUILD_APPEAL)
            );

        AsylumCase asylumCase =
            eventWithCaseData
                .getCaseData()
                .orElseThrow(() -> new IllegalStateException("caseData not present"));

        asylumCase
            .setLegalArgumentDocument(
                legalArgument.getDocument().orElse(null)
            );

        asylumCase
            .setLegalArgumentDescription(
                legalArgument.getDescription().orElse(null)
            );

        asylumCase
            .setLegalArgumentEvidence(
                legalArgument.getEvidence().orElse(null)
            );

        ccdAsylumCaseEventProcessor.completeEvent(
            caseId,
            eventWithCaseData
        );
    }
}
