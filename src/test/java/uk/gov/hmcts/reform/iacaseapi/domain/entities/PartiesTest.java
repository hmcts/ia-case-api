package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PartiesTest {

    @Test
    void has_correct_values() {
        assertEquals("legalRepresentative", Parties.LEGAL_REPRESENTATIVE.toString());
        assertEquals("respondent", Parties.RESPONDENT.toString());
        assertEquals("both", Parties.BOTH.toString());
        assertEquals("appellant", Parties.APPELLANT.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, Parties.values().length);
    }
}
