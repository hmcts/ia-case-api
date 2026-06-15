package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum BaseLocation {
    BIRMINGHAM("231596"),
    BRADFORD("698118"),
    GLASGOW("366559"),
    HATTON_CROSS("386417"),
    MANCHESTER("512401"),
    NEWPORT("227101"),
    TAYLOR_HOUSE("765324"),
    NORTH_SHIELDS("562808"),
    NEWCASTLE("366796"),
    ARNHEM_HOUSE("324339"),
    HARMONDSWORTH("28837"),

    YARLS_WOOD("649000"),
    ALLOA_SHERRIF("999971"),
    CROWN_HOUSE("420587"),
    IAC_NATIONAL_VIRTUAL("999970");

    @JsonValue
    private final String id;

    BaseLocation(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Optional<BaseLocation> fromId(String id) {
        for (BaseLocation location : BaseLocation.values()) {
            if (location.getId().equals(id)) {
                return Optional.of(location);
            }
        }
        return Optional.empty();
    }
}
