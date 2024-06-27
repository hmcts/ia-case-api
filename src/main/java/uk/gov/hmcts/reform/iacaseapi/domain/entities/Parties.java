package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Parties {

    LEGAL_REPRESENTATIVE("legalRepresentative"),
    RESPONDENT("respondent"),
    BOTH("both"), // Legal rep and respondent
    APPELLANT("appellant"),
    APPELLANT_AND_RESPONDENT("appellantAndRespondent");

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
