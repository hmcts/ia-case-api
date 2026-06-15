package uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;

import java.time.LocalDateTime;

@Data
public class CaseDetailsForTest {

    private long id;
    private String jurisdiction;
    private State state;
    @JsonProperty("case_data")
    private BailCase caseData;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    CaseDetailsForTest(long id, String jurisdiction, State state, BailCase caseData, LocalDateTime createdDate) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
        this.createdDate = createdDate;
    }

    public static class CaseDetailsForTestBuilder implements Builder<CaseDetailsForTest> {

        public static CaseDetailsForTestBuilder someCaseDetailsWith() {
            return new CaseDetailsForTestBuilder();
        }

        private long id = 1;
        private String jurisdiction = "ia";
        private State state;
        private BailCase caseData;
        private LocalDateTime createdDate = LocalDateTime.now();

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

        public CaseDetailsForTestBuilder caseData(BailCaseForTest caseData) {
            this.caseData = caseData.build();
            return this;
        }

        public CaseDetailsForTestBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CaseDetailsForTest build() {
            return new CaseDetailsForTest(id, jurisdiction, state, caseData, createdDate);
        }

        public String toString() {
            return "CaseDetailsForTest.CaseDetailsForTestBuilder(id=" + this.id + ", jurisdiction="
                + this.jurisdiction + ", state=" + this.state + ", caseData=" + this.caseData
                + ", createdDate=" + this.createdDate + ")";
        }
    }
}
