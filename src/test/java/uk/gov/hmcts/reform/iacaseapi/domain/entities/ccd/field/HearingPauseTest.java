package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class HearingPauseTest {

    private final String pauseStartTime = "10:15";
    private final String pauseEndTime = "10:45";

    private final HearingPause hearingPause = HearingPause.builder()
        .pauseStartTime(pauseStartTime)
        .pauseEndTime(pauseEndTime)
        .build();

    @Test
    void should_hold_onto_values() {

        assertEquals(pauseStartTime, hearingPause.getPauseStartTime());
        assertEquals(pauseEndTime, hearingPause.getPauseEndTime());
    }

    @Test
    void should_compare_by_value() {

        assertEquals(new HearingPause(pauseStartTime, pauseEndTime), hearingPause);
        assertNotEquals(new HearingPause(pauseStartTime, "11:00"), hearingPause);
    }
}
