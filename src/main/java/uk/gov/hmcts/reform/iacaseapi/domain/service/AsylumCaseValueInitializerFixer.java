package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumCaseValueInitializerFixer<T> implements DataFixer<AsylumCase> {

    private final AsylumCaseFieldDefinition field;
    private final T value;

    public AsylumCaseValueInitializerFixer(
        AsylumCaseFieldDefinition field,
        T value
    ) {
        this.field = field;
        this.value = value;
    }

    @Override
    public void fix(AsylumCase asylumCase) {

        if (!asylumCase.read(field).isPresent()) {
            asylumCase.write(field, value);
        }
    }
}
