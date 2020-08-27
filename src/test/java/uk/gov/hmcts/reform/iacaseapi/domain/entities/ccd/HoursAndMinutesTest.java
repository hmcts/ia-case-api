package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;

class HoursAndMinutesTest {

    @Test
    void should_create_hours_and_minutes() {

        HoursAndMinutes hoursAndMinutes = new HoursAndMinutes();

        assertNull(hoursAndMinutes.getHours());
        assertNull(hoursAndMinutes.getMinutes());

        hoursAndMinutes = new HoursAndMinutes("5", "30");

        assertEquals("5", hoursAndMinutes.getHours());
        assertEquals("30", hoursAndMinutes.getMinutes());
    }
}