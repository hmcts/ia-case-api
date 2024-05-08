package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class DirectionFinder {

    private DirectionFinder() {
        // prevent public constructor for Sonar
    }

    public static Optional<Direction> findFirst(
        AsylumCase asylumCase,
        DirectionTag directionTag
    ) {
        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase
                .read(AsylumCaseFieldDefinition.DIRECTIONS);
        return
            maybeDirections
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == directionTag)
                .findFirst();
    }

    public static String resolvePartiesForConfirmationBody(AsylumCase asylumCase, DirectionTag tag) {
        Optional<Direction> maybeExistingDirection = DirectionFinder.findFirst(asylumCase, tag);

        final boolean isDirectionToAppelant = maybeExistingDirection
                .map(direction -> direction.getParties().equals(Parties.APPELLANT))
                .orElseThrow(() -> new IllegalStateException("Parties are not present"));

        return isDirectionToAppelant
                ? "Appellant"
                : "Legal representative";
    }
}
