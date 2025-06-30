package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BaseLocationTest {

    @Test
    void has_correct_asylum_appeal_types_description() {

        assertEquals("231596", BaseLocation.BIRMINGHAM.getId());
        assertEquals("698118", BaseLocation.BRADFORD.getId());
        assertEquals("198444", BaseLocation.GLASGOW_DEPRECATED.getId());
        assertEquals("366559", BaseLocation.GLASGOW.getId());
        assertEquals("386417", BaseLocation.HATTON_CROSS.getId());
        assertEquals("512401", BaseLocation.MANCHESTER.getId());
        assertEquals("227101", BaseLocation.NEWPORT.getId());
        assertEquals("765324", BaseLocation.TAYLOR_HOUSE.getId());
        assertEquals("562808", BaseLocation.NORTH_SHIELDS.getId());
        assertEquals("366796", BaseLocation.NEWCASTLE.getId());
        assertEquals("324339", BaseLocation.ARNHEM_HOUSE.getId());
        assertEquals("28837", BaseLocation.HARMONDSWORTH.getId());
        assertEquals("649000", BaseLocation.YARLS_WOOD.getId());
        assertEquals("999971", BaseLocation.ALLOA_SHERRIF.getId());
        assertEquals("420587", BaseLocation.CROWN_HOUSE.getId());
        assertEquals("999970", BaseLocation.IAC_NATIONAL_VIRTUAL.getId());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(15, BaseLocation.values().length);
    }
}
