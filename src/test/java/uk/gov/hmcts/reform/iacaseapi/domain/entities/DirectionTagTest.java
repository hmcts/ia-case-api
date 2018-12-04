package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class DirectionTagTest {

    @Test
    public void has_correct_values() {
        assertEquals("buildCase", DirectionTag.BUILD_CASE.toString());
        assertEquals("legalRepresentativeReview", DirectionTag.LEGAL_REPRESENTATIVE_REVIEW.toString());
        assertEquals("legalRepresentativeHearingRequirements", DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS.toString());
        assertEquals("respondentEvidence", DirectionTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("respondentReview", DirectionTag.RESPONDENT_REVIEW.toString());
        assertEquals("", DirectionTag.NONE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, DirectionTag.values().length);
    }
}
