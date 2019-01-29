package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType.from;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType;

@Service
public class AppealReferenceNumberGenerator {

    private final EnumMap<AsylumAppealType, AppealReferenceNumber> lastAppealReferenceNumbers = new EnumMap<>(AsylumAppealType.class);
    private final AppealReferenceNumberInitializer appealReferenceNumberInitalizer;
    private final int appealReferenceSequenceSeed;
    private final DateProvider dateProvider;

    public AppealReferenceNumberGenerator(
        @Value("${appealReferenceSequenceSeed}") int appealReferenceSequenceSeed,
        AppealReferenceNumberInitializer appealReferenceNumberInitalizer,
        DateProvider dateProvider
    ) {
        this.appealReferenceNumberInitalizer = appealReferenceNumberInitalizer;
        this.appealReferenceSequenceSeed = appealReferenceSequenceSeed;
        this.dateProvider = dateProvider;
    }

    public synchronized Optional<String> getNextAppealReferenceNumberFor(String appealType) {

        if (lastAppealReferenceNumbers.isEmpty()) {

            Map<AsylumAppealType, AppealReferenceNumber> initialReferenceNumbers =
                appealReferenceNumberInitalizer.initialize();

            lastAppealReferenceNumbers.putAll(initialReferenceNumbers);
        }

        return incrementAndGetAppealReferenceNumberFor(appealType);
    }

    private Optional<String> incrementAndGetAppealReferenceNumberFor(String asylumAppealTypeString) {

        Optional<AsylumAppealType> maybeAsylumAppealType =
            from(asylumAppealTypeString);

        if (!maybeAsylumAppealType.isPresent()) {
            return Optional.empty();
        }

        AsylumAppealType asylumAppealType = maybeAsylumAppealType.get();
        String currentYear = String.valueOf(dateProvider.now().getYear());

        if (!yearOfLastReference(asylumAppealType).equals(currentYear)) {

            lastAppealReferenceNumbers.put(
                asylumAppealType,
                new AppealReferenceNumber(
                    asylumAppealType,
                    increment(appealReferenceSequenceSeed),
                    currentYear)
            );

        } else {
            incrementLastReference(asylumAppealType);
        }

        return Optional.of(
            lastAppealReferenceNumbers.get(asylumAppealType).toString()
        );
    }

    private int increment(int number) {
        return number + 1;
    }

    private String yearOfLastReference(AsylumAppealType asylumAppealType) {
        return this.lastAppealReferenceNumbers.get(asylumAppealType).getYear();
    }

    private void incrementLastReference(AsylumAppealType asylumAppealType) {
        
        AppealReferenceNumber lastAppealReferenceNumber =
            lastAppealReferenceNumbers.get(asylumAppealType);

        AppealReferenceNumber newAppealReferenceNumber =
            new AppealReferenceNumber(
                asylumAppealType,
                increment(lastAppealReferenceNumber.getSequence()),
                lastAppealReferenceNumber.getYear()
            );

        lastAppealReferenceNumbers.put(asylumAppealType, newAppealReferenceNumber);
    }
}
