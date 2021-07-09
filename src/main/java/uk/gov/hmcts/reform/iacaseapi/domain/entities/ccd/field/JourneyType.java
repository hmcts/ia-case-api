package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum  JourneyType {
    AIP("aip", "Appellant"),
    REP("rep", "Legal representative");

    @JsonValue
    private final String id;
    private final String label;

    JourneyType(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public String toString() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
