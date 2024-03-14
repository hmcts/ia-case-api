package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SourceOfRemittal {

    COURT_OF_APPEAL("Court Of Appeal"),
    UPPER_TRIBUNAL("Upper Tribunal");

    @JsonValue
    private final String value;

    SourceOfRemittal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}