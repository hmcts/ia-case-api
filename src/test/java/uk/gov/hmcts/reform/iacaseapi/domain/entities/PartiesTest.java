package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PartiesTest {

    @Test
    void has_correct_values() {
        assertEquals("Legal Representative", Parties.LEGAL_REPRESENTATIVE.toString());
        assertEquals("Respondent", Parties.RESPONDENT.toString());
        assertEquals("Both", Parties.BOTH.toString());
        assertEquals("Respondent and Appellant", Parties.RESPONDENT_AND_APPELLANT.toString());
        assertEquals("None", Parties.NONE.toString());
        assertEquals("Appellant", Parties.APPELLANT.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, Parties.values().length);
    }
}
