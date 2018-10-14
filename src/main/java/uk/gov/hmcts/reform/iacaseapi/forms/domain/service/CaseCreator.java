package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseCreator;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseEventProcessor;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.Event;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.EventWithCaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

@Service
public class CaseCreator {

    private final CcdAsylumCaseCreator ccdAsylumCaseCreator;
    private final CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor;

    public CaseCreator(
        @Autowired CcdAsylumCaseCreator ccdAsylumCaseCreator,
        CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor
    ) {
        this.ccdAsylumCaseCreator = ccdAsylumCaseCreator;
        this.ccdAsylumCaseEventProcessor = ccdAsylumCaseEventProcessor;
    }

    public CaseDetails<AsylumCase> create(
        AsylumCase asylumCase
    ) {
        CaseDetails<AsylumCase> startedAsylumCaseDetails =
            startAppeal(asylumCase);

        CaseDetails<AsylumCase> submittedAsylumCaseDetails =
            submitAppeal(startedAsylumCaseDetails);

        return submittedAsylumCaseDetails;
    }

    private CaseDetails<AsylumCase> startAppeal(
        AsylumCase asylumCase
    ) {
        EventWithCaseData<AsylumCase> eventWithCaseData =
            ccdAsylumCaseCreator.startEvent(
                new Event(EventId.START_APPEAL)
            );

        eventWithCaseData.setCaseData(asylumCase);

        return ccdAsylumCaseCreator.completeEvent(eventWithCaseData);
    }

    private CaseDetails<AsylumCase> submitAppeal(
        CaseDetails<AsylumCase> asylumCaseCaseDetails
    ) {
        EventWithCaseData<AsylumCase> eventWithCaseData =
            ccdAsylumCaseEventProcessor.startEvent(
                String.valueOf(asylumCaseCaseDetails.getId()),
                new Event(EventId.SUBMIT_APPEAL)
            );

        eventWithCaseData.setCaseData(
            asylumCaseCaseDetails.getCaseData()
        );

        return ccdAsylumCaseEventProcessor.completeEvent(
            String.valueOf(asylumCaseCaseDetails.getId()),
            eventWithCaseData
        );
    }
}
