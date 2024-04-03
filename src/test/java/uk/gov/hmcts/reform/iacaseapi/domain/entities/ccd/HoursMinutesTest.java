package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HoursMinutesTest {

    @Test
    void should_instantiate_with_values() {
        HoursMinutes object = new HoursMinutes(2, 10);
        assertEquals(2, object.getHours());
        assertEquals(10, object.getMinutes());
    }

    @Test
    void should_instantiate_with_minutes_alone() {
        HoursMinutes object1 = new HoursMinutes(0, 80);
        assertEquals(1, object1.getHours());
        assertEquals(20, object1.getMinutes());

        HoursMinutes object2 = new HoursMinutes(80);
        assertEquals(1, object1.getHours());
        assertEquals(20, object1.getMinutes());
    }

    @Test
    void should_instantiate_with_null_values() {
        HoursMinutes object1 = new HoursMinutes(2, null);
        assertEquals(2, object1.getHours());
        assertEquals(0, object1.getMinutes());

        HoursMinutes object2 = new HoursMinutes(null, 15);
        assertEquals(0, object2.getHours());
        assertEquals(15, object2.getMinutes());

        HoursMinutes object3 = new HoursMinutes(null, null);
        assertEquals(0, object3.getHours());
        assertEquals(0, object3.getMinutes());
    }

    @Test
    void should_convert_time_in_total_minutes() {
        HoursMinutes object = new HoursMinutes(3, 10);
        assertEquals(190, object.convertToIntegerMinutes());
    }
}
