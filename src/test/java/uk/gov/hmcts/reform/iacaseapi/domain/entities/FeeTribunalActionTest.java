package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FeeTribunalActionTest {

    @Test
    void has_correct_values() {
        assertEquals("refund", FeeTribunalAction.REFUND.toString());
        assertEquals("additionalPayment", FeeTribunalAction.ADDITIONAL_PAYMENT.toString());
        assertEquals("noAction", FeeTribunalAction.NO_ACTION.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, FeeTribunalAction.values().length);
    }
}
