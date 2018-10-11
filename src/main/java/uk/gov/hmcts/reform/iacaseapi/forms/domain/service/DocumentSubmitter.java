package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseEventProcessor;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.Event;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.EventWithCaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

@Service
public class DocumentSubmitter {

    private final CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor;

    public DocumentSubmitter(
        @Autowired CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor
    ) {
        this.ccdAsylumCaseEventProcessor = ccdAsylumCaseEventProcessor;
    }

    public void submit(
        final String caseId,
        DocumentWithMetadata document
    ) {
        EventWithCaseData<AsylumCase> eventWithCaseData =
            ccdAsylumCaseEventProcessor.startEvent(
                caseId,
                new Event(EventId.UPLOAD_DOCUMENT)
            );

        AsylumCase asylumCase = eventWithCaseData.getCaseData();

        asylumCase
            .setDocument(document);

        ccdAsylumCaseEventProcessor.completeEvent(
            caseId,
            eventWithCaseData
        );
    }
}
