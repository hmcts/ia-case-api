package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DecideFtpaApplicationType {
    PARTIALLY_GRANTED("partiallyGranted"),
    GRANTED("granted"),
    REFUSED("refused"),
    APPLICATION_NOT_ADMITTED("notAdmitted"),
    WITHDRAWN("withdrawn"),
    REHEARD_RULE35("reheardRule35"),
    REMADE_RULE31("remadeRule31"),
    REMADE_RULE32("remadeRule32");

    @JsonValue
    private final String id;

    DecideFtpaApplicationType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
