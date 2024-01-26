package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DecideFtpaApplicationType {
    PARTIALLY_GRANTED("partiallyGranted"),
    GRANTED("granted"),
    REFUSED("refused"),
    REHEARD_RULE35("reheardRule35"),
    REHEARD_RULE32("reheardRule32"),
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
