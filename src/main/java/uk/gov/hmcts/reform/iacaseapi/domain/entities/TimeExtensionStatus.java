package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TimeExtensionStatus {
    IN_PROGRESS("inProgress"),
    SUBMITTED("submitted");

    @JsonValue
    private final String id;

    TimeExtensionStatus(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
