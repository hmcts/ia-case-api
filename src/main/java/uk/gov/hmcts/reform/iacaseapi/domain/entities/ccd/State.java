package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {

    APPEAL_STARTED("appealStarted"),
    APPEAL_SUBMITTED("appealSubmitted"),
    APPEAL_SUBMITTED_OUT_OF_TIME("appealSubmittedOutOfTime"),
    AWAITING_RESPONDENT_EVIDENCE("awaitingRespondentEvidence"),
    CASE_BUILDING("caseBuilding"),
    CASE_UNDER_REVIEW("caseUnderReview"),
    RESPONDENT_REVIEW("respondentReview"),
    SUBMIT_HEARING_REQUIREMENTS("submitHearingRequirements"),
    LISTING("listing"),
    PREPARE_FOR_HEARING("prepareForHearing"),
    FINAL_BUNDLING("finalBundling"),
    PRE_HEARING("preHearing"),
    DECISION("decision"),
    DECIDED("decided"),
    ENDED("ended"),

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



