package uk.gov.hmcts.reform.iacaseapi.integration.util;

import java.time.LocalDateTime;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseBuilder;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;

public class TestAsylumCaseBuilder {

    private String appealReference;

    private TestAsylumCaseBuilder() {
    }

    public static TestAsylumCaseBuilder anAsylumCase() {
        return new TestAsylumCaseBuilder();
    }

    public TestAsylumCaseBuilder withAppealReference(String reference) {
        this.appealReference = reference;
        return this;
    }

    public CaseDetails<AsylumCase> build() {

        AsylumCaseBuilder asylumCaseBuilder = new AsylumCaseBuilder();
        asylumCaseBuilder.setAppealReferenceNumber(Optional.of(appealReference));
        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        CaseDetails<AsylumCase> asylumCaseDetails = new CaseDetails<>(
            1,
            "some-jurisdiction",
            State.APPEAL_SUBMITTED,
            asylumCase,
            LocalDateTime.now()
        );

        return asylumCaseDetails;
    }
}
