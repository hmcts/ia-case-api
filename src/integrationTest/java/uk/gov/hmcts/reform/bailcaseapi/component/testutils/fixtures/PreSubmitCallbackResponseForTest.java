package uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;

@Value
public class PreSubmitCallbackResponseForTest {

    @JsonProperty
    private BailCase data;
    @JsonProperty
    private Set<String> errors;

    @JsonCreator
    public PreSubmitCallbackResponseForTest(
        @JsonProperty("data") BailCase data,
        @JsonProperty("errors") Set<String> errors
    ) {
        this.data = data;
        this.errors = errors;
    }

    public BailCase getBailCase() {
        return data;
    }

    public static class PreSubmitCallbackResponseForTestBuilder implements Builder<PreSubmitCallbackResponseForTest> {

        public static PreSubmitCallbackResponseForTestBuilder someCallbackResponseWith() {
            return new PreSubmitCallbackResponseForTestBuilder();
        }

        private BailCaseForTest data;
        private Set<String> errors;

        public PreSubmitCallbackResponseForTestBuilder data(BailCaseForTest data) {
            this.data = data;
            return this;
        }

        public PreSubmitCallbackResponseForTestBuilder errors(Set<String> errors) {
            this.errors = errors;
            return this;
        }

        public PreSubmitCallbackResponseForTest build() {
            return new PreSubmitCallbackResponseForTest(data.build(), errors);
        }

        public String toString() {
            return "PreSubmitCallbackResponseForTest.PreSubmitCallbackResponseForTestBuilder(data="
                + this.data + ", errors=" + this.errors + ")";
        }
    }
}
