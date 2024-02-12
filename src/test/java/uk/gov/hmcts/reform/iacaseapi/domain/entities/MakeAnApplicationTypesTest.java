package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MakeAnApplicationTypesTest {

    @Test
    void has_correct_values() {
        assertEquals("Adjourn", MakeAnApplicationTypes.ADJOURN.toString());
        assertEquals("Expedite", MakeAnApplicationTypes.EXPEDITE.toString());
        assertEquals("Link/unlink appeals", MakeAnApplicationTypes.LINK_OR_UNLINK.toString());
        assertEquals("Judge's review of application decision", MakeAnApplicationTypes.JUDGE_REVIEW.toString());
        assertEquals("Time extension", MakeAnApplicationTypes.TIME_EXTENSION.toString());
        assertEquals("Transfer", MakeAnApplicationTypes.TRANSFER.toString());
        assertEquals("Withdraw", MakeAnApplicationTypes.WITHDRAW.toString());
        assertEquals("Update hearing requirements", MakeAnApplicationTypes.UPDATE_HEARING_REQUIREMENTS.toString());
        assertEquals("Update appeal details", MakeAnApplicationTypes.UPDATE_APPEAL_DETAILS.toString());
        assertEquals("Reinstate an ended appeal", MakeAnApplicationTypes.REINSTATE.toString());
        assertEquals("Other", MakeAnApplicationTypes.OTHER.toString());
        assertEquals("Application under rule 31 or rule 32", MakeAnApplicationTypes.APPLICATION_UNDER_RULE_31_OR_RULE_32.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(12, MakeAnApplicationTypes.values().length);
    }
}
