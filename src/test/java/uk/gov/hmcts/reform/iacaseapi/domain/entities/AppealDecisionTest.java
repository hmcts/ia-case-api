package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppealDecisionTest {

    @Test
    void has_correct_values() {
        assertEquals(AppealDecision.DISMISSED.toString(), AppealDecision.DISMISSED.getValue());
        assertEquals(AppealDecision.ALLOWED.toString(), AppealDecision.ALLOWED.getValue());

        assertEquals("dismissed", AppealDecision.DISMISSED.toString());
        assertEquals("allowed", AppealDecision.ALLOWED.toString());

        assertEquals(AppealDecision.DISMISSED, AppealDecision.from("dismissed"));
        assertEquals(AppealDecision.ALLOWED, AppealDecision.from("allowed"));
    }

    @Test
    void should_throw_exception_when_name_unrecognised() {
        assertThatThrownBy(() -> AppealDecision.from("unknown"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("unknown not an AppealDecision")
            .hasNoCause();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, AppealDecision.values().length);
    }

}
