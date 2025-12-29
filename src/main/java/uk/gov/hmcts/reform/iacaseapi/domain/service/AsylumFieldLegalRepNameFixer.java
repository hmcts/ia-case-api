package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@Component
public class AsylumFieldLegalRepNameFixer implements DataFixer<AsylumCase> {

    @Override
    public void fix(AsylumCase asylumCase) {
        if (hasNoFamilyName(asylumCase)) {

            Optional<String> legalRepName = asylumCase.read(LEGAL_REP_NAME, String.class);

            if (legalRepName.isPresent()) {
                String[] fullName = legalRepName.get().split(" ", 2);
                asylumCase.write(LEGAL_REP_NAME, fullName[0]);

                asylumCase.write(LEGAL_REP_FAMILY_NAME, fullName.length > 1 ? fullName[1] : " ");
            }

        }
    }

    private boolean hasNoFamilyName(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class).orElse("").trim().isEmpty();
    }
}
