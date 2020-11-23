package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class RejectionReasonTest {

    private RejectionReason rejectionReason;

    @Test
    public void has_correct_values_after_setting() {
        rejectionReason = new RejectionReason("some-reason");
        assertNotNull(rejectionReason);
        assertEquals("some-reason", rejectionReason.getReason());
    }

}
