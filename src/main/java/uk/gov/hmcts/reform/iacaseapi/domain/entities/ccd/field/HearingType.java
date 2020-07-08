package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HearingType {
    HEARING("hearing"),
    APPOINTMENT("appointment");

    @JsonValue
    private final String id;

    HearingType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
