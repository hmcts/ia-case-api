package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum State {

    APPEAL_STARTED("appealStarted", "Appeal started"),
    APPEAL_STARTED_BY_ADMIN("appealStartedByAdmin", "Appeal started by admin"),
    APPEAL_SUBMITTED("appealSubmitted", "Appeal submitted"),
    APPEAL_SUBMITTED_OUT_OF_TIME("appealSubmittedOutOfTime", "Appeal submitted out of time"),
    PENDING_PAYMENT("pendingPayment", "Payment pending"),
    AWAITING_RESPONDENT_EVIDENCE("awaitingRespondentEvidence", "Awaiting respondent evidence"),
    CASE_BUILDING("caseBuilding", "Case building"),
    CASE_UNDER_REVIEW("caseUnderReview", "Case under review"),
    RESPONDENT_REVIEW("respondentReview", "Respondent review"),
    SUBMIT_HEARING_REQUIREMENTS("submitHearingRequirements", "Submit hearing requirements"),
    LISTING("listing", "Listing"),
    PREPARE_FOR_HEARING("prepareForHearing", "Prepare for hearing"),
    FINAL_BUNDLING("finalBundling", "Final bundling"),
    PRE_HEARING("preHearing", "Pre hearing"),
    DECISION("decision", "Decision"),
    DECIDED("decided", "Decided"),
    ENDED("ended", "Ended"),
    APPEAL_TAKEN_OFFLINE("appealTakenOffline", "Appeal taken offline"),
    AWAITING_REASONS_FOR_APPEAL("awaitingReasonsForAppeal", "Awaiting reasons for appeal"),
    REASONS_FOR_APPEAL_SUBMITTED("reasonsForAppealSubmitted", "Reasons for appeal submitted"),
    FTPA_SUBMITTED("ftpaSubmitted", "FTPA Submitted"),
    FTPA_DECIDED("ftpaDecided", "FTPA Decided"),
    AWAITING_CLARIFYING_QUESTIONS_ANSWERS("awaitingClarifyingQuestionsAnswers", "Awaiting clarifying questions answers"),
    CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED("clarifyingQuestionsAnswersSubmitted", "Clarifying questions answers submitted"),
    AWAITING_CMA_REQUIREMENTS("awaitingCmaRequirements", "Awaiting Cma requirements"),
    CMA_REQUIREMENTS_SUBMITTED("cmaRequirementsSubmitted", "Cma requirements submitted"),
    CMA_ADJUSTMENTS_AGREED("cmaAdjustmentsAgreed", "Cma adjustments agreed"),
    CMA_LISTED("cmaListed", "Cma listed"),
    ADJOURNED("adjourned", "Adjourned"),
    REMITTED("remitted", "Remitted"),
    MIGRATED("migrated", "Migrated"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown", "Unknown");

    @JsonValue
    private final String id;
    private final String description;

    State(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String toString() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<State> getStateFrom(String s) {
        return Arrays.stream(State.values())
            .filter(v -> v.id.equals(s))
            .findFirst();
    }
}



