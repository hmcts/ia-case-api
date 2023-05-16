package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Parties {

    LEGAL_REPRESENTATIVE("Legal Representative"),
    RESPONDENT("Respondent"),
    BOTH("Both"), // Legal Representative and Respondent
    RESPONDENT_AND_APPELLANT("Respondent and Appellant"),
    NONE("None"),
    APPELLANT("Appellant");

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
