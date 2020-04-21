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
    private String requestedDate;
    private String reason;
    private State state;
    private TimeExtensionStatus status;
    private List<IdValue<Document>> evidence;
    private TimeExtensionDecision decision;
    private String decisionReason;

    private TimeExtension() {
    }

    public TimeExtension(String requestedDate, String reason, State state, TimeExtensionStatus status, List<IdValue<Document>> evidence) {
        this(requestedDate, reason, state, status, evidence, null, null);
    }

    public TimeExtension(String requestedDate, String reason, State state, TimeExtensionStatus status, List<IdValue<Document>> evidence, TimeExtensionDecision decision, String decisionReason) {
        this.requestedDate = requestedDate;
        this.reason = reason;
        this.state = state;
        this.status = status;
        this.evidence = evidence;
        this.decision = decision;
        this.decisionReason = decisionReason;
    }

    public String getRequestedDate() {
        return requestedDate;
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
}
