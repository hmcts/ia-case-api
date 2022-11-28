package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DetentionFacilityTest {

    @Test
    void test_all_values() {
        assertEquals(DetentionFacility.IRC.getValue(), "immigrationRemovalCentre");
        assertEquals(DetentionFacility.PRISON.getValue(), "prison");
        assertEquals(DetentionFacility.OTHER.getValue(), "other");
    }

    @Test
    void test_toString_gives_same_value() {
        assertEquals(DetentionFacility.IRC.getValue(), DetentionFacility.IRC.toString());
        assertEquals(DetentionFacility.PRISON.getValue(), DetentionFacility.PRISON.toString());
        assertEquals(DetentionFacility.OTHER.getValue(), DetentionFacility.OTHER.toString());
    }

    @Test
    void should_throw_error_for_unknown_value() {
        assertThatThrownBy(() -> DetentionFacility.from("unknown"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("unknown not a Detention Facility");
    }

    @Test
    void should_create_one_facility_from_given_value() {
        assertEquals(DetentionFacility.IRC, DetentionFacility.from("immigrationRemovalCentre"));
        assertEquals(DetentionFacility.PRISON, DetentionFacility.from("prison"));
        assertEquals(DetentionFacility.OTHER, DetentionFacility.from("other"));
    }

    @Test
    void should_break_if_new_facility_is_added() {
        assertEquals(3, DetentionFacility.values().length);
    }

}
