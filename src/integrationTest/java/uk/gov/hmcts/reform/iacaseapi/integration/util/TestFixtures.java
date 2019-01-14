package uk.gov.hmcts.reform.iacaseapi.integration.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.RP;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseBuilder;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;

@SuppressWarnings("unchecked")
public class TestFixtures {

    private TestFixtures() {
    }

    public static List<CaseDetails<AsylumCase>> anEmptyListOfCaseDetails() {
        return Collections.emptyList();
    }

    public static List<CaseDetails<AsylumCase>> someListOfCasesIncludingButPriorTo(TestAsylumCaseBuilder latestAsylumCaseBuilder) {

        int seed = getSequenceNumber(
                latestAsylumCaseBuilder.build()
                        .getCaseData()
                        .getAppealReferenceNumber()
                        .get()
        );

        List<CaseDetails<AsylumCase>> caseDetails = rangeClosed((seed - 20), (seed))
                .mapToObj(i -> buildCase(i, (i % 2 == 0 ? RP : PA)))
                .collect(toList());

        caseDetails.forEach(cd -> System.out.println(cd.getCaseData().getAppealReferenceNumber()));

        caseDetails.add(latestAsylumCaseBuilder.build());

        return caseDetails;
    }

    private static CaseDetails<AsylumCase> buildCase(int sequence, AsylumAppealType caseType) {

        AsylumCaseBuilder caseBuilder = new AsylumCaseBuilder();
        caseBuilder.setAppealType(Optional.of(caseType.getValue()));
        caseBuilder.setAppealReferenceNumber(Optional.of(caseType.name() + "/" + sequence + "/2018"));
        CaseDetails<AsylumCase> caseDetails =
                new CaseDetails<>(nextLong(), "IA", APPEAL_STARTED, new AsylumCase(caseBuilder));

        return caseDetails;
    }

    private static int getSequenceNumber(String appealReference) {
        return Integer.valueOf(appealReference.split("/")[1]);
    }
}
