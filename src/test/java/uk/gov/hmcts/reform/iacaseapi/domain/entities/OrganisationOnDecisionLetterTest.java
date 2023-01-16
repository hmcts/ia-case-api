package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OrganisationOnDecisionLetterTest {

    @Test
    void has_correct_values() {
        assertEquals("localAuthority", OrganisationOnDecisionLetter.LOCAL_AUTHORITY.toString());
        assertEquals("nationalAgeAssessmentBoard", OrganisationOnDecisionLetter.NATIONAL_AGE_ASSESSMENT_BOARD.toString());
        assertEquals("hscTrust", OrganisationOnDecisionLetter.HSC_TRUST.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, OrganisationOnDecisionLetter.values().length);
    }
}
