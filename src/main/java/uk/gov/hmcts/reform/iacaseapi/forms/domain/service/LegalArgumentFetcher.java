package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.api.CcdAsylumCaseFetcher;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.LegalArgument;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;

@Service
public class LegalArgumentFetcher {

    private final CcdAsylumCaseFetcher asylumCaseFetcher;

    public LegalArgumentFetcher(
        @Autowired CcdAsylumCaseFetcher asylumCaseFetcher
    ) {
        this.asylumCaseFetcher = asylumCaseFetcher;
    }

    public LegalArgument fetch(
        String caseId
    ) {
        CaseDetails<AsylumCase> asylumCaseDetails = asylumCaseFetcher.fetch(caseId);

        AsylumCase asylumCase = asylumCaseDetails.getCaseData();

        Document legalArgumentDocument =
            asylumCase
                .getLegalArgumentDocument()
                .orElse(null);

        String legalArgumentDescription =
            asylumCase
                .getLegalArgumentDescription()
                .orElse(null);

        Documents legalArgumentEvidence =
            asylumCase
                .getLegalArgumentEvidence()
                .orElse(null);

        return new LegalArgument(
            legalArgumentDocument,
            legalArgumentDescription,
            legalArgumentEvidence
        );
    }
}
