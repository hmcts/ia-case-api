package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TimeExtensionStatus {
    IN_PROGRESS("inProgress"),
    SUBMITTED("submitted"),
    GRANTED("granted"),
    REFUSED("refused");

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
