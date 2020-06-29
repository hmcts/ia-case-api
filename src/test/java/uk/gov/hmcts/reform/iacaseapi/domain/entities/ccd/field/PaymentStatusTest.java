package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class PaymentStatusTest {

    @Test
    public void has_correct_values() {
        assertEquals("Paid", PaymentStatus.PAID.toString());
        assertEquals("Payment due", PaymentStatus.PAYMENT_DUE.toString());
        assertEquals("Failed", PaymentStatus.FAILED.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, PaymentStatus.values().length);
    }
}
