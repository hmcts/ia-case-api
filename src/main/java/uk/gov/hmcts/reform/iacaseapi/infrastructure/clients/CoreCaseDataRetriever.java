package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.AsylumCaseRetrievalException;

@Service
public class CoreCaseDataRetriever {

    private final AsylumCasesRetriever asylumCasesRetriever;

    public CoreCaseDataRetriever(AsylumCasesRetriever asylumCasesRetriever) {
        this.asylumCasesRetriever = asylumCasesRetriever;
    }


    public List<Map> retrieveAppealCasesInAllStatesExceptAppealStarted() {

        List<Map> asylumCaseDetails;

        try {
            asylumCaseDetails = rangeClosed(1, asylumCasesRetriever.getNumberOfPages())
                    .parallel()
                    .mapToObj(String::valueOf)
                    .flatMap(pageParam -> asylumCasesRetriever.getAsylumCasesPage(pageParam).stream())
                    .collect(toList());

        } catch (AsylumCaseRetrievalException exp) {
            throw new AsylumCaseRetrievalException("Couldn't retrieve appeal cases from Ccd", exp);
        }

        return asylumCaseDetails;
    }
}
