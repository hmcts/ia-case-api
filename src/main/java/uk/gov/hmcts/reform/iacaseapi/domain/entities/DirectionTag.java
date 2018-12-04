package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum DirectionTag {

    BUILD_CASE("buildCase"),
    LEGAL_REPRESENTATIVE_REVIEW("legalRepresentativeReview"),
    LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS("legalRepresentativeHearingRequirements"),
    RESPONDENT_EVIDENCE("respondentEvidence"),
    RESPONDENT_REVIEW("respondentReview"),

    @JsonEnumDefaultValue
    NONE("");

    private final String id;

    DirectionTag(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
