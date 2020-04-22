package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class TimeExtension {
    private String requestDate;
    private String reason;
    private State state;
    private TimeExtensionStatus status;
    private List<IdValue<Document>> evidence;
    private TimeExtensionDecision decision;
    private String decisionReason;
    private String decisionOutcomeDate;

    private TimeExtension() {
    }

    public TimeExtension(String requestDate, String reason, State state, TimeExtensionStatus status, List<IdValue<Document>> evidence) {
        this(requestDate, reason, state, status, evidence, null, null, null);
    }

    public TimeExtension(String requestDate, String reason, State state, TimeExtensionStatus status, List<IdValue<Document>> evidence, TimeExtensionDecision decision, String decisionReason, String decisionOutcomeDate) {
        this.requestDate = requestDate;
        this.reason = reason;
        this.state = state;
        this.status = status;
        this.evidence = evidence;
        this.decision = decision;
        this.decisionReason = decisionReason;
        this.decisionOutcomeDate = decisionOutcomeDate;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getReason() {
        return reason;
    }

    public State getState() {
        return state;
    }

    public TimeExtensionStatus getStatus() {
        return status;
    }

    public List<IdValue<Document>> getEvidence() {
        return evidence;
    }

    public TimeExtensionDecision getDecision() {
        return decision;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public String getDecisionOutcomeDate() {
        return decisionOutcomeDate;
    }
}
