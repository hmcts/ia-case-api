package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AppealStatusTest {

    @Test
    public void has_correct_asylum_appeal_status() {
        assertEquals(AppealStatus.REINSTATED.toString(), AppealStatus.REINSTATED.getValue());
        assertEquals("Reinstated", AppealStatus.REINSTATED.toString());
        assertEquals(AppealStatus.REINSTATED, AppealStatus.from("Reinstated"));
    }

    @Test
    public void should_throw_exception_when_appeal_status_unrecognised() {

        assertThatThrownBy(() -> AppealStatus.from("unknown"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("unknown not an AppealStatus")
            .hasNoCause();
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(1, AppealStatus.values().length);
    }
}
