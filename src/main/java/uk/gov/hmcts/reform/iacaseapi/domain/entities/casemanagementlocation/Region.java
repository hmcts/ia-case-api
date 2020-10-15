package uk.gov.hmcts.reform.iacaseapi.domain.entities.casemanagementlocation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Region {
    NATIONAL("1", "National");

    @JsonValue
    private final String id;
    @JsonValue
    private final String name;

    Region(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
