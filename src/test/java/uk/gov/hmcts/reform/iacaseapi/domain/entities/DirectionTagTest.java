package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DirectionTagTest {

    @Test
    void has_correct_values() {
        assertEquals("buildCase", DirectionTag.BUILD_CASE.toString());
        assertEquals("caseEdit", DirectionTag.CASE_EDIT.toString());
        assertEquals("legalRepresentativeReview", DirectionTag.LEGAL_REPRESENTATIVE_REVIEW.toString());
        assertEquals("legalRepresentativeHearingRequirements", DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS.toString());
        assertEquals("respondentEvidence", DirectionTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("respondentReview", DirectionTag.RESPONDENT_REVIEW.toString());
        assertEquals("requestCaseBuilding", DirectionTag.REQUEST_CASE_BUILDING.toString());
        assertEquals("requestResponseReview", DirectionTag.REQUEST_RESPONSE_REVIEW.toString());
        assertEquals("requestResponseAmend", DirectionTag.REQUEST_RESPONSE_AMEND.toString());
        assertEquals("requestReasonsForAppeal", DirectionTag.REQUEST_REASONS_FOR_APPEAL.toString());
        assertEquals("requestClarifyingQuestions", DirectionTag.REQUEST_CLARIFYING_QUESTIONS.toString());
        assertEquals("requestCmaRequirements", DirectionTag.REQUEST_CMA_REQUIREMENTS.toString());
        assertEquals("", DirectionTag.NONE.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(13, DirectionTag.values().length);
    }
}
