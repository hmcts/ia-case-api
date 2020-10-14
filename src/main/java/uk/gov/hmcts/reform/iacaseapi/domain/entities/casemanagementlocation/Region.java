package uk.gov.hmcts.reform.iacaseapi.domain.entities.casemanagementlocation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Region {
    NATIONAL("1");

    @JsonValue
    private final String listElementCode;

    Region(String listElementCode) {
        this.listElementCode = listElementCode;
    }
}
