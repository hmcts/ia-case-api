package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreviousListingDetailsTest {

    private final ListingEvent listingEvent = ListingEvent.INITIAL_LISTING;
    private final ListingHearingCentre listingLocation = ListingHearingCentre.BIRMINGHAM;
    private final String listingHearingDate = "2023-02-21";
    private final String listingHearingDuration = "100";

    private final PreviousListingDetails previousListingDetails = new PreviousListingDetails(
        listingEvent,
        listingLocation,
        listingHearingDate,
        listingHearingDuration
    );

    @Test
    void should_hold_onto_values() {
        assertEquals(listingEvent, previousListingDetails.getListingEvent());
        assertEquals(listingLocation, previousListingDetails.getListingLocation());
        assertEquals(listingHearingDate, previousListingDetails.getListingHearingDate());
        assertEquals(listingHearingDuration, previousListingDetails.getListingHearingDuration());
    }

}
