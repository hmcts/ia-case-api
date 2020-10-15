package uk.gov.hmcts.reform.iacaseapi.domain.entities.casemanagementlocation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BaseLocation {
    BIRMINGHAM("231596", "Birmingham"),
    BRADFORD("698118", "Bradford"),
    GLASGOW("198444", "Glasgow"),
    HATTON_CROSS("386417", "Hatton Cross"),
    MANCHESTER("512401", "Manchester"),
    NEWCASTLE("", ""),
    NEWPORT("227101", "Newport"),
    TAYLOR_HOUSE("765324", "Taylor House");

    @JsonValue
    private final String id;
    @JsonValue
    private final String name;

    BaseLocation(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
