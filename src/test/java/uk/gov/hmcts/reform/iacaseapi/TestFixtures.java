package uk.gov.hmcts.reform.iacaseapi;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;

import java.time.LocalDateTime;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseBuilder;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@SuppressWarnings("unchecked")
public class TestFixtures {

    private TestFixtures() {
    }

    public static Callback<AsylumCase> submitAppealCallbackForProtectionAsylumCase(
        Event event
    ) {
        CaseDetails<AsylumCase> caseDetails = buildCase(nextInt(), PA);
        Callback<AsylumCase> asylumCaseCallback =
            new Callback<>(caseDetails, Optional.empty(), event);

        asylumCaseCallback
            .getCaseDetails()
            .getCaseData()
            .setAppealReferenceNumber(null);

        return asylumCaseCallback;
    }

    private static CaseDetails<AsylumCase> buildCase(int sequence, AsylumAppealType caseType) {

        AsylumCaseBuilder caseBuilder = new AsylumCaseBuilder();
        caseBuilder.setAppealType(Optional.of(caseType.getValue()));

        caseBuilder.setAppealReferenceNumber(
            Optional.of(caseType.name() + "/" + sequence + "/2018"));

        CaseDetails<AsylumCase> caseDetails =
            new CaseDetails<>(123, "IA", APPEAL_STARTED, new AsylumCase(caseBuilder), LocalDateTime.now());

        return caseDetails;
    }
}
