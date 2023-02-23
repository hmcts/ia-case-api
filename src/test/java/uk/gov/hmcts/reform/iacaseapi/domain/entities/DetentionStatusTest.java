package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DetentionStatusTest {

    @Test
    void has_correct_detained_detention_status() {
        assertEquals(DetentionStatus.DETAINED.toString(), DetentionStatus.DETAINED.getValue());
        assertEquals("detained", DetentionStatus.DETAINED.toString());
        assertEquals(DetentionStatus.DETAINED, DetentionStatus.from("detained"));
    }

    @Test
    void has_correct_accelerated_detention_status() {
        assertEquals(DetentionStatus.ACCELERATED.toString(), DetentionStatus.ACCELERATED.getValue());
        assertEquals("detainedAccelerated", DetentionStatus.ACCELERATED.toString());
        assertEquals(DetentionStatus.ACCELERATED, DetentionStatus.from("detainedAccelerated"));
    }

    @Test
    void should_throw_exception_when_detention_status_unrecognised() {

        assertThatThrownBy(() -> DetentionStatus.from("unknown"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("unknown not a Detention Status")
            .hasNoCause();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, DetentionStatus.values().length);
    }
}