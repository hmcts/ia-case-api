package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DetentionFacility {

    IRC("immigrationRemovalCentre"),
    PRISON("prison"),
    OTHER("other");

    @JsonValue
    private String value;

    DetentionFacility(String value) {
        this.value = value;
    }

    public static DetentionFacility from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " not a Detention Facility"));
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
