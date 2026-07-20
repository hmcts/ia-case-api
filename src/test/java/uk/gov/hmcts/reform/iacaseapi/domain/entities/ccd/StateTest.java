package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;

class StateTest {

    @Test
    void has_correct_values() {
        assertEquals("appealStarted", APPEAL_STARTED.toString());
        assertEquals("appealStartedByAdmin", APPEAL_STARTED_BY_ADMIN.toString());
        assertEquals("appealSubmitted", APPEAL_SUBMITTED.toString());
        assertEquals("pendingPayment", PENDING_PAYMENT.toString());
        assertEquals("awaitingRespondentEvidence", AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("caseBuilding", CASE_BUILDING.toString());
        assertEquals("caseUnderReview", CASE_UNDER_REVIEW.toString());
        assertEquals("respondentReview", RESPONDENT_REVIEW.toString());
        assertEquals("submitHearingRequirements", SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("listing", LISTING.toString());
        assertEquals("prepareForHearing", PREPARE_FOR_HEARING.toString());
        assertEquals("finalBundling", FINAL_BUNDLING.toString());
        assertEquals("preHearing", PRE_HEARING.toString());
        assertEquals("decision", DECISION.toString());
        assertEquals("decided", DECIDED.toString());
        assertEquals("ended", ENDED.toString());
        assertEquals("appealTakenOffline", APPEAL_TAKEN_OFFLINE.toString());
        assertEquals("ftpaSubmitted", FTPA_SUBMITTED.toString());
        assertEquals("ftpaDecided", FTPA_DECIDED.toString());
        assertEquals("awaitingClarifyingQuestionsAnswers", AWAITING_CLARIFYING_QUESTIONS_ANSWERS.toString());
        assertEquals("clarifyingQuestionsAnswersSubmitted", CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED.toString());
        assertEquals("awaitingCmaRequirements", AWAITING_CMA_REQUIREMENTS.toString());
        assertEquals("cmaRequirementsSubmitted", CMA_REQUIREMENTS_SUBMITTED.toString());
        assertEquals("adjourned", ADJOURNED.toString());
        assertEquals("cmaAdjustmentsAgreed", CMA_ADJUSTMENTS_AGREED.toString());
        assertEquals("cmaListed", CMA_LISTED.toString());
        assertEquals("remitted", REMITTED.toString());
        assertEquals("migrated", MIGRATED.toString());
        assertEquals("unknown", State.UNKNOWN.toString());

        assertEquals("Appeal started", APPEAL_STARTED.getDescription());
        assertEquals("Appeal started by admin", APPEAL_STARTED_BY_ADMIN.getDescription());
        assertEquals("Appeal submitted", APPEAL_SUBMITTED.getDescription());
        assertEquals("Payment pending", PENDING_PAYMENT.getDescription());
        assertEquals("Awaiting respondent evidence", AWAITING_RESPONDENT_EVIDENCE.getDescription());
        assertEquals("Case building", CASE_BUILDING.getDescription());
        assertEquals("Case under review", CASE_UNDER_REVIEW.getDescription());
        assertEquals("Respondent review", RESPONDENT_REVIEW.getDescription());
        assertEquals("Submit hearing requirements", SUBMIT_HEARING_REQUIREMENTS.getDescription());
        assertEquals("Listing", LISTING.getDescription());
        assertEquals("Prepare for hearing", PREPARE_FOR_HEARING.getDescription());
        assertEquals("Final bundling", FINAL_BUNDLING.getDescription());
        assertEquals("Pre hearing", PRE_HEARING.getDescription());
        assertEquals("Decision", DECISION.getDescription());
        assertEquals("Decided", DECIDED.getDescription());
        assertEquals("Ended", ENDED.getDescription());
        assertEquals("Appeal taken offline", APPEAL_TAKEN_OFFLINE.getDescription());
        assertEquals("Awaiting reasons for appeal", AWAITING_REASONS_FOR_APPEAL.getDescription());
        assertEquals("Reasons for appeal submitted", REASONS_FOR_APPEAL_SUBMITTED.getDescription());
        assertEquals("FTPA Submitted", FTPA_SUBMITTED.getDescription());
        assertEquals("FTPA Decided", FTPA_DECIDED.getDescription());
        assertEquals("Awaiting clarifying questions answers", AWAITING_CLARIFYING_QUESTIONS_ANSWERS.getDescription());
        assertEquals("Clarifying questions answers submitted", CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED.getDescription());
        assertEquals("Awaiting Cma requirements", AWAITING_CMA_REQUIREMENTS.getDescription());
        assertEquals("Cma requirements submitted", CMA_REQUIREMENTS_SUBMITTED.getDescription());
        assertEquals("Cma adjustments agreed", CMA_ADJUSTMENTS_AGREED.getDescription());
        assertEquals("Cma listed", CMA_LISTED.getDescription());
        assertEquals("Adjourned", ADJOURNED.getDescription());
        assertEquals("Remitted", REMITTED.getDescription());
        assertEquals("Migrated", MIGRATED.getDescription());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(32, State.values().length);
    }

    @Test
    void should_get_state_from_string() {
        Arrays.stream(State.values()).forEach(state ->
            assertEquals(state, State.getStateFrom(state.toString()).orElse(null))
        );
    }
}
