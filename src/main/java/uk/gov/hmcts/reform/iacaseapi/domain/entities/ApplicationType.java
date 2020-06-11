package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationType {

    TIME_EXTENSION("Time extension"),
    ADJOURN("Adjourn"),
    EXPEDITE("Expedite"),
    TRANSFER("Transfer"),
    WITHDRAW("Withdraw"),
    UPDATE_HEARING_REQUIREMENTS("Update hearing requirements"),
    UPDATE_CMA_APPOINTMENT_DETAILS("Update Cma appointment details"),
    CHANGE_HEARING_CENTRE("Change hearing centre");

    @JsonValue
    private final String value;

    ApplicationType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
