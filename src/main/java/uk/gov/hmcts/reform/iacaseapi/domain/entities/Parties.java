package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Parties {

    LEGAL_REPRESENTATIVE("legalRepresentative"),
    RESPONDENT("respondent"),
    BOTH("both"),
    APPELLANT("appellant");

    @JsonValue
    private final String id;

    Parties(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
