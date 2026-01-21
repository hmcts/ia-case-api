package uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;

import java.util.Map;

public class BailCaseForTest implements Builder<BailCase> {

    private BailCase bailCase = new BailCase();

    public static BailCaseForTest anBailCase() {
        return new BailCaseForTest();
    }

    public BailCaseForTest withCaseDetails(BailCase bailCase) {
        this.bailCase.putAll(bailCase);
        return this;
    }

    public <T> BailCaseForTest with(BailCaseFieldDefinition field, T value) {
        bailCase.write(field, value);
        return this;
    }

    public BailCaseForTest writeOrOverwrite(Map<String, Object> additionalBailCaseData) {
        bailCase.putAll(additionalBailCaseData);
        return this;
    }

    public BailCase build() {
        return bailCase;
    }
}
