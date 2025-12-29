package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {
    APPLICATION_STARTED("applicationStarted"),
    APPLICATION_STARTED_BY_LR("applicationStartedByLR"),
    APPLICATION_STARTED_BY_HO("applicationStartedByHO"),
    APPLICATION_ENDED("applicationEnded"),
    APPLICATION_SUBMITTED("applicationSubmitted"),
    BAIL_SUMMARY_UPLOADED("bailSummaryUploaded"),
    UNSIGNED_DECISION("unsignedDecision"),
    DECISION_DECIDED("decisionDecided"),
    DECISION_CONDITIONAL_BAIL("decisionConditionalBail"),
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    State(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
