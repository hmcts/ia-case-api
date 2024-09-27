package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OutOfCountryCircumstances {

    ENTRY_CLEARANCE_DECISION("entryClearanceDecision"),
    LEAVE_UK("leaveUk"),
    NONE("none");

    @JsonValue
    private final String value;

    OutOfCountryCircumstances(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
