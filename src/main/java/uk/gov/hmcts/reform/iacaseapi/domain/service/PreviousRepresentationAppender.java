package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousRepresentation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class PreviousRepresentationAppender {

    public List<IdValue<PreviousRepresentation>> append(
        List<IdValue<PreviousRepresentation>> existingPreviousRepresentations,
        PreviousRepresentation newPreviousRepresentation
    ) {
        requireNonNull(existingPreviousRepresentations, "existingPreviousRepresentations must not be null");
        requireNonNull(newPreviousRepresentation, "newPreviousRepresentation must not be null");

        final List<IdValue<PreviousRepresentation>> allPreviousRepresentations = new ArrayList<>();

        int index = existingPreviousRepresentations.size() + 1;

        allPreviousRepresentations.add(new IdValue<>(String.valueOf(index--), newPreviousRepresentation));

        for (IdValue<PreviousRepresentation> existingPreviousRepresentation : existingPreviousRepresentations) {
            allPreviousRepresentations.add(new IdValue<>(String.valueOf(index--), existingPreviousRepresentation.getValue()));
        }

        return allPreviousRepresentations;
    }
}
