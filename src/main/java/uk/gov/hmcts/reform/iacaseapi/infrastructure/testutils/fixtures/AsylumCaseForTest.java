package uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures;

import java.util.Map;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumCaseForTest implements Builder<AsylumCase> {

    private AsylumCase asylumCase = new AsylumCase();

    public static AsylumCaseForTest anAsylumCase() {
        return new AsylumCaseForTest();
    }

    public AsylumCaseForTest withCaseDetails(AsylumCase asylumCase) {
        this.asylumCase.putAll(asylumCase);
        return this;
    }

    public <T> AsylumCaseForTest with(AsylumCaseFieldDefinition field, T value) {
        asylumCase.write(field, value);
        return this;
    }

    public AsylumCaseForTest writeOrOverwrite(Map<String, Object> additionalAsylumCaseData) {
        asylumCase.putAll(additionalAsylumCaseData);
        return this;
    }

    public AsylumCase build() {
        return asylumCase;
    }
}
