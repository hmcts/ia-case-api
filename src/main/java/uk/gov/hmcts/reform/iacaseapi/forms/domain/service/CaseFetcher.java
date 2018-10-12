package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseFetcher;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;

@Service
public class CaseFetcher {

    private final CcdAsylumCaseFetcher asylumCaseFetcher;

    public CaseFetcher(
        @Autowired CcdAsylumCaseFetcher asylumCaseFetcher
    ) {
        this.asylumCaseFetcher = asylumCaseFetcher;
    }

    public AsylumCase fetch(
        String caseId
    ) {
        CaseDetails<AsylumCase> asylumCaseDetails = asylumCaseFetcher.fetch(caseId);

        return asylumCaseDetails
            .getCaseData();
    }
}
