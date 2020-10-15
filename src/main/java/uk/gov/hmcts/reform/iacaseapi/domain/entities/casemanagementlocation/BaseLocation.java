package uk.gov.hmcts.reform.iacaseapi.domain.entities.casemanagementlocation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BaseLocation {
    BIRMINGHAM("231596"),
    BRADFORD("698118"),
    COVENTRY("698118"),
    GLASGOW("698118"),
    GLASGOW_TRIBUNALS_CENTRE("698118"),
    HATTON_CROSS("698118"),
    MANCHESTER("698118"),
    NEWCASTLE("698118"),
    NEWPORT("698118"),
    NORTH_SHIELDS("698118"),
    NOTTINGHAM("698118"),
    TAYLOR_HOUSE("765324");

    @JsonValue
    private final String id;

    BaseLocation(String id) {
        this.id = id;
    }
}
