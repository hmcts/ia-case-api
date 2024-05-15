package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class StateTest {

    @Test
    void has_correct_values() {
        assertEquals("appealStarted", State.APPEAL_STARTED.toString());
        assertEquals("appealStartedByAdmin", State.APPEAL_STARTED_BY_ADMIN.toString());
        assertEquals("appealSubmitted", State.APPEAL_SUBMITTED.toString());
        assertEquals("appealSubmittedOutOfTime", State.APPEAL_SUBMITTED_OUT_OF_TIME.toString());
        assertEquals("pendingPayment", State.PENDING_PAYMENT.toString());
        assertEquals("awaitingRespondentEvidence", State.AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("caseBuilding", State.CASE_BUILDING.toString());
        assertEquals("caseUnderReview", State.CASE_UNDER_REVIEW.toString());
        assertEquals("respondentReview", State.RESPONDENT_REVIEW.toString());
        assertEquals("submitHearingRequirements", State.SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("listing", State.LISTING.toString());
        assertEquals("prepareForHearing", State.PREPARE_FOR_HEARING.toString());
        assertEquals("finalBundling", State.FINAL_BUNDLING.toString());
        assertEquals("preHearing", State.PRE_HEARING.toString());
        assertEquals("decision", State.DECISION.toString());
        assertEquals("decided", State.DECIDED.toString());
        assertEquals("ended", State.ENDED.toString());
        assertEquals("appealTakenOffline", State.APPEAL_TAKEN_OFFLINE.toString());
        assertEquals("ftpaSubmitted", State.FTPA_SUBMITTED.toString());
        assertEquals("ftpaDecided", State.FTPA_DECIDED.toString());
        assertEquals("awaitingClarifyingQuestionsAnswers", State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS.toString());
        assertEquals("clarifyingQuestionsAnswersSubmitted", State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED.toString());
        assertEquals("awaitingCmaRequirements", State.AWAITING_CMA_REQUIREMENTS.toString());
        assertEquals("cmaRequirementsSubmitted", State.CMA_REQUIREMENTS_SUBMITTED.toString());
        assertEquals("adjourned", State.ADJOURNED.toString());
        assertEquals("cmaAdjustmentsAgreed", State.CMA_ADJUSTMENTS_AGREED.toString());
        assertEquals("cmaListed", State.CMA_LISTED.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(30, State.values().length);
    }

    @Test
    void should_get_state_from_string() {
        Arrays.stream(State.values()).forEach(state ->
            assertEquals(state, State.getStateFrom(state.toString()).orElse(null))
        );
    }
}
