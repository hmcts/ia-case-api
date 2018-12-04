package uk.gov.hmcts.reform.iacaseapi;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.PA;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseBuilder;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;

@SuppressWarnings("unchecked")
public class TestFixtures {

    private TestFixtures() {
    }

    public static Callback<AsylumCase> submitAppealCallbackForProtectionAsylumCase() {

        CaseDetails caseDetails = buildCase(nextInt(), PA);
        Callback<AsylumCase> asylumCaseCallback =
            new Callback<>(caseDetails, Optional.empty(), SUBMIT_APPEAL);

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
            new CaseDetails<>(nextLong(), "IA", APPEAL_STARTED, new AsylumCase(caseBuilder));

        return caseDetails;
    }
}
