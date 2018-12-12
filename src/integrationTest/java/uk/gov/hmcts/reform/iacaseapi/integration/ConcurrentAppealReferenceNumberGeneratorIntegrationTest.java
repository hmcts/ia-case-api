package uk.gov.hmcts.reform.iacaseapi.integration;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.RP;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import java.util.concurrent.*;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;
import uk.gov.hmcts.reform.iacaseapi.integration.stubs.CcdMock;
import uk.gov.hmcts.reform.iacaseapi.integration.util.IdamStubbedSpringBootIntegrationTest;

public class ConcurrentAppealReferenceNumberGeneratorIntegrationTest extends IdamStubbedSpringBootIntegrationTest {

    @Autowired
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    @Autowired
    private Serializer<List> ccdCaseListSerializer;

    private CcdMock givenCcd;

    @Before
    public void createCcdMock() {
        givenCcd = new CcdMock(ccdCaseListSerializer);
    }

    @Test
    public void generates_appeal_reference_numbers_concurrently() throws JsonProcessingException, InterruptedException {

        givenCcd.doesntHaveAnyExistingAppealCases();

        List<String> generatedAppealReferenceNumbers = andLotsOfClientsConcurrentlyRequestAppealReferenceNumbers();

        Assertions.assertThat(
                removeDuplicates(generatedAppealReferenceNumbers).size())
                    .isEqualTo(generatedAppealReferenceNumbers.size());
    }

    private List<String> andLotsOfClientsConcurrentlyRequestAppealReferenceNumbers() throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1000);

        List<Callable<Optional<String>>> referenceNumberRequests = range(0, 1000)
                .mapToObj(this::alternatingAsylumTypeAppealReferenceNumberRequest)
                .collect(toList());

        List<Future<Optional<String>>> futureMaybeReferenceNumber =
                executor.invokeAll(referenceNumberRequests);

        return futureMaybeReferenceNumber
                .parallelStream()
                .map(this::getMaybeReferenceNumber)
                .map(Optional::get)
                .collect(toList());
    }

    private Callable<Optional<String>> alternatingAsylumTypeAppealReferenceNumberRequest(int i) {
        return i % 2 == 0
                ? callableRevocationOfProtectionAppealReferenceNumberRequest() :
                callableProtectionAppealReferenceNumberRequest();
    }

    private <T> Set<T> removeDuplicates(Collection<T> generatedAppealReferenceNumbers) {
        return new HashSet<>(generatedAppealReferenceNumbers);
    }

    private Optional<String> getMaybeReferenceNumber(Future<Optional<String>> f) {
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Callable<Optional<String>> callableRevocationOfProtectionAppealReferenceNumberRequest() {
        return () -> appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(RP.toString());
    }

    private Callable<Optional<String>> callableProtectionAppealReferenceNumberRequest() {
        return () -> appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(PA.toString());
    }
}
