package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListingEventTest {

    @Test
    void has_correct_values() {
        assertEquals("initialListing", ListingEvent.INITIAL_LISTING.toString());
        assertEquals("relisting", ListingEvent.RELISTING.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ListingEvent.values().length);
    }
}
