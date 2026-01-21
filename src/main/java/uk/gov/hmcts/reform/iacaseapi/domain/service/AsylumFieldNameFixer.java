package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumFieldNameFixer implements DataFixer<AsylumCase> {

    private final AsylumCaseFieldDefinition fromField;
    private final AsylumCaseFieldDefinition toField;

    public AsylumFieldNameFixer(
        AsylumCaseFieldDefinition fromField,
        AsylumCaseFieldDefinition toField
    ) {
        this.fromField = fromField;
        this.toField = toField;
    }

    @Override
    public void fix(AsylumCase asylumCase) {

        Optional<Object> valueToBeTransitioned = asylumCase.read(fromField);

        if (valueToBeTransitioned.isPresent()) {

            asylumCase.write(toField, valueToBeTransitioned);

            asylumCase.clear(fromField);
        }
    }
}
