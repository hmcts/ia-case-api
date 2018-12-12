package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.from;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.AsylumCaseRetrievalException;

@Service
public class AppealReferenceNumberGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AppealReferenceNumberGenerator.class);

    private final Map<AsylumAppealType, AppealReferenceNumber> lastAppealReferenceNumbers = new HashMap<>();
    private final AppealReferenceNumberInitializer appealReferenceNumberInitalizer;
    private final String appealReferenceSequenceSeed;
    private final DateProvider dateProvider;

    public AppealReferenceNumberGenerator(
            @Value("appealReferenceSequenceSeed") String appealReferenceSequenceSeed,
            AppealReferenceNumberInitializer appealReferenceNumberInitalizer,
            DateProvider dateProvider) {

        this.appealReferenceNumberInitalizer = appealReferenceNumberInitalizer;
        this.appealReferenceSequenceSeed = appealReferenceSequenceSeed;
        this.dateProvider = dateProvider;
    }

    public synchronized Optional<String> getNextAppealReferenceNumberFor(String appealType) {

        Optional<String> nextReferenceNumber = Optional.empty();

        if (lastAppealReferenceNumbers.isEmpty()) {
            try {
                Map<AsylumAppealType, AppealReferenceNumber> referenceNumberMap =
                        appealReferenceNumberInitalizer.initialize();

                lastAppealReferenceNumbers.putAll(referenceNumberMap);

                nextReferenceNumber =
                        incrementAndGetAppealReferenceNumberFor(appealType);

            } catch (AsylumCaseRetrievalException e) {
                LOG.error(e.getMessage());
            }
        } else {
            nextReferenceNumber =
                    incrementAndGetAppealReferenceNumberFor(appealType);
        }

        return nextReferenceNumber;
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
                            currentYear));
        } else {
            incrementLastReference(asylumAppealType);
        }

        return Optional.of(
                lastAppealReferenceNumbers.get(asylumAppealType).toString());
    }

    private String increment(String intString) {
        return String.valueOf(Integer.valueOf(intString) + 1);
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
                        lastAppealReferenceNumber.getYear());

        lastAppealReferenceNumbers.put(asylumAppealType, newAppealReferenceNumber);
    }
}
