package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Parties {

    APPLICANT("applicant"),
    LEGAL_REPRESENTATIVE("legalRepresentative"),
    HOME_OFFICE("homeOffice");

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
