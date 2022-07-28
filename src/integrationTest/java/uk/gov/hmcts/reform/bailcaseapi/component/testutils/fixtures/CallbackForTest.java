package uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;

public class CallbackForTest {

    @JsonProperty("event_id")
    private Event event;
    @JsonProperty("case_details")
    private CaseDetailsForTest caseDetails;

    CallbackForTest(Event event, CaseDetailsForTest caseDetails) {
        this.event = event;
        this.caseDetails = caseDetails;
    }

    public static class CallbackForTestBuilder implements Builder<CallbackForTest> {

        public static CallbackForTestBuilder callback() {
            return new CallbackForTestBuilder();
        }

        private Event event;
        private CaseDetailsForTest caseDetails;

        public CallbackForTestBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public CallbackForTestBuilder caseDetails(CaseDetailsForTest.CaseDetailsForTestBuilder caseDetails) {
            this.caseDetails = caseDetails.build();
            return this;
        }

        public CallbackForTest build() {
            return new CallbackForTest(event, caseDetails);
        }
    }
}
