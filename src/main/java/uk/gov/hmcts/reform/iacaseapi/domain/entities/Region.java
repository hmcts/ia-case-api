package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Region {
    NATIONAL("1");

    @JsonValue
    private String id;

    Region(String id) {
        this.id = id;
    }
}
