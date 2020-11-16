package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class PreviousRequirementsAndRequestsAppender {

    public List<IdValue<DocumentWithMetadata>> append(
        List<IdValue<DocumentWithMetadata>> existingPreviousRequirementsAndRequests,
        DocumentWithMetadata currentRequirementsAndRequests
    ) {
        requireNonNull(existingPreviousRequirementsAndRequests, "existingRequirementsAndRequests must not be null");
        requireNonNull(currentRequirementsAndRequests, "newRequirementsAndRequests must not be null");

        final List<IdValue<DocumentWithMetadata>> allPreviousRequirementsAndRequests = new ArrayList<>();

        int index = existingPreviousRequirementsAndRequests.size() + 1;

        allPreviousRequirementsAndRequests.add(new IdValue<>(String.valueOf(index--), currentRequirementsAndRequests));

        for (IdValue<DocumentWithMetadata> existingPreviousRequirementsAndRequest : existingPreviousRequirementsAndRequests) {
            allPreviousRequirementsAndRequests.add(new IdValue<>(String.valueOf(index--), existingPreviousRequirementsAndRequest.getValue()));
        }

        return allPreviousRequirementsAndRequests;
    }

    public void appendAndTrim(AsylumCase asylumCase) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybePreviousRequirementsAndRequests =
            asylumCase.read(PREVIOUS_HEARING_REQUIREMENTS);

        final List<IdValue<DocumentWithMetadata>> existingPreviousRequirementsAndRequests =
            maybePreviousRequirementsAndRequests.orElse(Collections.emptyList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeRequirementsAndRequests =
            asylumCase.read(HEARING_REQUIREMENTS);

        final List<IdValue<DocumentWithMetadata>> existingRequirementsAndRequests =
            maybeRequirementsAndRequests.orElse(Collections.emptyList());

        if ((long) existingRequirementsAndRequests.size() >= 1) {

            final DocumentWithMetadata currentRequirementAndRequest =
                existingRequirementsAndRequests.get(existingRequirementsAndRequests.size() - 1).getValue();

            List<IdValue<DocumentWithMetadata>> allPreviousRequirementsAndRequests =
                append(
                    existingPreviousRequirementsAndRequests,
                    currentRequirementAndRequest);

            asylumCase.write(PREVIOUS_HEARING_REQUIREMENTS, allPreviousRequirementsAndRequests);

            existingRequirementsAndRequests.subList(0, existingRequirementsAndRequests.size()).clear();

            asylumCase.write(HEARING_REQUIREMENTS, existingRequirementsAndRequests);
        }
    }
}
