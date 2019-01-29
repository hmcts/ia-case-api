package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberInitializer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CoreCaseDataRetriever;

@Service
public class AppealReferenceNumberInitializerFromCcd implements AppealReferenceNumberInitializer {

    private static final String CASE_DATA_KEY = "case_data";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";

    private final EnumMap<AsylumAppealType, AppealReferenceNumber> initialReferenceNumbers = new EnumMap<>(AsylumAppealType.class);
    private final CoreCaseDataRetriever coreCaseDataRetriever;
    private final DateProvider dateProvider;
    private final int appealReferenceSequenceSeed;

    public AppealReferenceNumberInitializerFromCcd(
        CoreCaseDataRetriever coreCaseDataRetriever,
        DateProvider dateProvider,
        @Value("${appealReferenceSequenceSeed}") int appealReferenceSequenceSeed
    ) {
        this.coreCaseDataRetriever = coreCaseDataRetriever;
        this.dateProvider = dateProvider;
        this.appealReferenceSequenceSeed = appealReferenceSequenceSeed;
    }

    public synchronized Map<AsylumAppealType, AppealReferenceNumber> initialize() {

        if (initialReferenceNumbers.isEmpty()) {

            List<Map> asylumCases = coreCaseDataRetriever.retrieveAllAppealCases();
            List<Map> filteredAsylumCases = removeCasesWithoutAnAppealReferenceNumber(asylumCases);

            initializeFromSeed();
            initializeFromExistingAsylumCases(filteredAsylumCases);
        }

        return initialReferenceNumbers;
    }

    private void initializeFromExistingAsylumCases(List<Map> asylumCases) {

        asylumCases
            .stream()
            .map(this::extractReferenceNumber)
            .collect(groupingBy(AppealReferenceNumber::getType))
            .forEach((key, appealReferenceNumbers) -> {
                appealReferenceNumbers.sort(new AppealReferenceNumberComparator());
                initialReferenceNumbers.put(key, appealReferenceNumbers.get(0));
            });
    }

    private void initializeFromSeed() {

        final String currentYear = String.valueOf(dateProvider.now().getYear());

        stream(AsylumAppealType.values())
            .forEach(asylumAppealType ->
                initialReferenceNumbers.put(
                    asylumAppealType,
                    new AppealReferenceNumber(
                        asylumAppealType,
                        appealReferenceSequenceSeed,
                        currentYear
                    )
                )
            );
    }

    private List<Map> removeCasesWithoutAnAppealReferenceNumber(List<Map> asylumCases) {
        return asylumCases.stream()
            .filter(map -> map.get(CASE_DATA_KEY) instanceof Map)
            .filter(map -> ((Map) map.get(CASE_DATA_KEY)).get(APPEAL_REFERENCE_NUMBER_KEY) != null)
            .collect(toList());
    }

    private AppealReferenceNumber extractReferenceNumber(Map appealMap) {
        String appealReferenceNumber =
            (String) ((Map) appealMap.get(CASE_DATA_KEY)).get(APPEAL_REFERENCE_NUMBER_KEY);

        return new AppealReferenceNumber(appealReferenceNumber);
    }
}
