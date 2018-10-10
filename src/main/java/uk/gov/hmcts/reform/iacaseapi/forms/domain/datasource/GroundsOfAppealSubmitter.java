package uk.gov.hmcts.reform.iacaseapi.forms.domain.datasource;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseEventProcessor;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.Event;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.EventWithCaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.GroundOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

@Service
public class GroundsOfAppealSubmitter {

    private final CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor;

    public GroundsOfAppealSubmitter(
        @Autowired CcdAsylumCaseEventProcessor ccdAsylumCaseEventProcessor
    ) {
        this.ccdAsylumCaseEventProcessor = ccdAsylumCaseEventProcessor;
    }

    public void subhmit(
        final String caseId,
        List<GroundOfAppeal> groundsOfAppeal
    ) {
        EventWithCaseData<AsylumCase> eventWithCaseData =
            ccdAsylumCaseEventProcessor.startEvent(
                caseId,
                new Event(
                    EventId.EDIT_GROUNDS_OF_APPEAL,
                    "Edit grounds of appeal",
                    "Edit grounds of appeal"
                )
            );

        List<String> appealGrounds =
            groundsOfAppeal
                .stream()
                .map(GroundOfAppeal::getGround)
                .collect(Collectors.toList());

        eventWithCaseData
            .getCaseData()
            .setAppealGrounds(appealGrounds);

        ccdAsylumCaseEventProcessor.completeEvent(
            caseId,
            eventWithCaseData
        );
    }
}
