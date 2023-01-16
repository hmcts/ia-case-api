package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrganisationOnDecisionLetter {

    LOCAL_AUTHORITY("localAuthority"),
    NATIONAL_AGE_ASSESSMENT_BOARD("nationalAgeAssessmentBoard"),
    HSC_TRUST("hscTrust");

    @JsonValue
    private final String id;

    OrganisationOnDecisionLetter(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
