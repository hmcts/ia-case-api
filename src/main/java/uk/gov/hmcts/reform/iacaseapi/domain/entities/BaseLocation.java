package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BaseLocation {
    BIRMINGHAM("231596"),
    BRADFORD("698118"),
    GLASGOW("198444"),
    HATTON_CROSS("386417"),
    MANCHESTER("512401"),
    NEWPORT("227101"),
    TAYLOR_HOUSE("765324"),
    NORTH_SHIELDS("562808");

    @JsonValue
    private final String id;

    BaseLocation(String id) {
        this.id = id;
    }
}
