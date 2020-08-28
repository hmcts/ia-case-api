package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HearingLengthTest {

    @Test
    void has_correct_values() {
        assertEquals(30, HearingLength.LENGTH_30_MINUTES.getValue());
        assertEquals(60, HearingLength.LENGTH_1_HOUR.getValue());
        assertEquals(90, HearingLength.LENGTH_1_HOUR_30_MINUTES.getValue());
        assertEquals(120, HearingLength.LENGTH_2_HOURS.getValue());
        assertEquals(150, HearingLength.LENGTH_2_HOURS_30_MINUTES.getValue());
        assertEquals(180, HearingLength.LENGTH_3_HOURS.getValue());
        assertEquals(210, HearingLength.LENGTH_3_HOURS_30_MINUTES.getValue());
        assertEquals(240, HearingLength.LENGTH_4_HOURS.getValue());
        assertEquals(270, HearingLength.LENGTH_4_HOURS_30_MINUTES.getValue());
        assertEquals(300, HearingLength.LENGTH_5_HOURS.getValue());
        assertEquals(330, HearingLength.LENGTH_5_HOURS_30_MINUTES.getValue());
        assertEquals(360, HearingLength.LENGTH_6_HOURS.getValue());
    }

    @Test
    void can_be_created_from() {
        assertEquals(HearingLength.from(30).get(), HearingLength.LENGTH_30_MINUTES);
        assertEquals(HearingLength.from(360).get(), HearingLength.LENGTH_6_HOURS);
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(12, HearingLength.values().length);
    }
}
