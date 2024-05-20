package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {

    APPEAL_STARTED("appealStarted"),
    APPEAL_STARTED_BY_ADMIN("appealStartedByAdmin"),
    APPEAL_SUBMITTED("appealSubmitted"),
    APPEAL_SUBMITTED_OUT_OF_TIME("appealSubmittedOutOfTime"),
    PENDING_PAYMENT("pendingPayment"),
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
    APPEAL_TAKEN_OFFLINE("appealTakenOffline"),
    AWAITING_REASONS_FOR_APPEAL("awaitingReasonsForAppeal"),
    REASONS_FOR_APPEAL_SUBMITTED("reasonsForAppealSubmitted"),
    FTPA_SUBMITTED("ftpaSubmitted"),
    FTPA_DECIDED("ftpaDecided"),
    AWAITING_CLARIFYING_QUESTIONS_ANSWERS("awaitingClarifyingQuestionsAnswers"),
    CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED("clarifyingQuestionsAnswersSubmitted"),
    AWAITING_CMA_REQUIREMENTS("awaitingCmaRequirements"),
    CMA_REQUIREMENTS_SUBMITTED("cmaRequirementsSubmitted"),
    CMA_ADJUSTMENTS_AGREED("cmaAdjustmentsAgreed"),
    CMA_LISTED("cmaListed"),
    ADJOURNED("adjourned"),
    REMITTED("remitted"),

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



