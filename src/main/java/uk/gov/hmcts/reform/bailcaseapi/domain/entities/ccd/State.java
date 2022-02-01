package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {
    APPLICATION_STARTED("applicationStarted"),
    APPLICATION_ENDED("applicationEnded"),
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    State(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
