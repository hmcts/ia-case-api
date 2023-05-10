package uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;

@Data
public class CaseDetailsForTest {

    private long id;
    private String jurisdiction;
    private State state;
    @JsonProperty("case_data")
    private AsylumCase caseData;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonProperty("supplementary_data")
    private Map<String,String> supplementaryData;

    CaseDetailsForTest(long id, String jurisdiction, State state, AsylumCase caseData, LocalDateTime createdDate, Map<String,String> supplementaryData) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
        this.createdDate = createdDate;
        this.supplementaryData = supplementaryData;
    }

    public static class CaseDetailsForTestBuilder implements Builder<CaseDetailsForTest> {

        public static CaseDetailsForTestBuilder someCaseDetailsWith() {
            return new CaseDetailsForTestBuilder();
        }

        private long id = 1;
        private String jurisdiction = "ia";
        private State state;
        private AsylumCase caseData;
        private LocalDateTime createdDate = LocalDateTime.now();
        private Map<String,String> supplementaryData = Map.of("HMCTSServiceId","BFA1");

        CaseDetailsForTestBuilder() {
        }

        public CaseDetailsForTestBuilder id(long id) {
            this.id = id;
            return this;
        }

        public CaseDetailsForTestBuilder jurisdiction(String jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public CaseDetailsForTestBuilder state(State state) {
            this.state = state;
            return this;
        }

        public CaseDetailsForTestBuilder caseData(AsylumCaseForTest caseData) {
            this.caseData = caseData.build();
            return this;
        }

        public CaseDetailsForTestBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CaseDetailsForTestBuilder supplementaryData(Map<String,String> supplementaryData) {
            this.supplementaryData = supplementaryData;
            return this;
        }

        public CaseDetailsForTest build() {
            return new CaseDetailsForTest(id, jurisdiction, state, caseData, createdDate, supplementaryData);
        }

        public String toString() {
            return "CaseDetailsForTest.CaseDetailsForTestBuilder(id=" + this.id + ", jurisdiction=" + this.jurisdiction + ", state=" + this.state + ", caseData=" + this.caseData + ", createdDate=" + this.createdDate + ", supplementaryData=" + this.supplementaryData + ")";
        }
    }
}
