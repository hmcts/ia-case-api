package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class FeeUpdateReasonTest {

    @Test
    void has_correct_values() {
        assertEquals("decisionTypeChanged", FeeUpdateReason.DECISION_TYPE_CHANGED.toString());
        assertEquals("appealNotValid", FeeUpdateReason.APPEAL_NOT_VALID.toString());
        assertEquals("feeRemissionChanged", FeeUpdateReason.FEE_REMISSION_CHANGED.toString());
        assertEquals("appealWithdrawn", FeeUpdateReason.APPEAL_WITHDRAWN.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, FeeUpdateReason.values().length);
    }
}
