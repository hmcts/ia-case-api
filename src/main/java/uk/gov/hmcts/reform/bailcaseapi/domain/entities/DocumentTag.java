package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentTag {

    GROUNDS_FOR_BAIL_EVIDENCE("uploadTheBailEvidenceDocs"),

    @JsonEnumDefaultValue
    NONE("");

    @JsonValue
    private final String id;

    DocumentTag(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
