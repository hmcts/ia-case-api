package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HearingAdjournmentDay {

    ON_HEARING_DATE("onHearingDate"),
    BEFORE_HEARING_DATE("beforeHearingDate");

    @JsonValue
    private final String value;

    HearingAdjournmentDay(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value ;
    }
}
