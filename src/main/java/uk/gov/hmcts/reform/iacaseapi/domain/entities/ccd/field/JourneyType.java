package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum  JourneyType {
    AIP("aip"),
    REP("rep");

    @JsonValue
    private final String id;

    JourneyType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
