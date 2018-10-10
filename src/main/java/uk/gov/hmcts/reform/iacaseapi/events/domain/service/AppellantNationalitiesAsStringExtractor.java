package uk.gov.hmcts.reform.iacaseapi.events.domain.service;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

@Service
public class AppellantNationalitiesAsStringExtractor {

    public Optional<String> extract(
        AsylumCase asylumCase
    ) {
        String nationalities = "";

        if (asylumCase
            .getAppellantNationalities()
            .isPresent()) {

            nationalities =
                asylumCase
                    .getAppellantNationalities()
                    .get()
                    .stream()
                    .map(IdValue::getValue)
                    .collect(Collectors.joining(", "));
        }

        if (asylumCase
            .getAppellantNationalityContested()
            .orElse("")
            .equals("Yes")) {

            if (!nationalities.isEmpty()) {
                nationalities += ", ";
            }

            nationalities += "(nationality is contested)";
        }

        if (nationalities.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(nationalities);
        }
    }
}
