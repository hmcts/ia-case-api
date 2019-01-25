package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CoreCaseDataRetriever {

    private final AsylumCasesRetriever asylumCasesRetriever;

    public CoreCaseDataRetriever(AsylumCasesRetriever asylumCasesRetriever) {
        this.asylumCasesRetriever = asylumCasesRetriever;
    }

    public List<Map> retrieveAllAppealCases() {

        List<Map> asylumCaseDetails;

        asylumCaseDetails = rangeClosed(1, asylumCasesRetriever.getNumberOfPages())
            .mapToObj(String::valueOf)
            .flatMap(pageParam -> asylumCasesRetriever.getAsylumCasesPage(pageParam).stream())
            .collect(toList());

        return asylumCaseDetails;
    }
}
