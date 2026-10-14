package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AppealReviewOutcome {

    DECISION_MAINTAINED("decisionMaintained"),
    DECISION_WITHDRAWN("decisionWithdrawn");

    @JsonValue
    private final String value;

    @JsonCreator
    AppealReviewOutcome(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}