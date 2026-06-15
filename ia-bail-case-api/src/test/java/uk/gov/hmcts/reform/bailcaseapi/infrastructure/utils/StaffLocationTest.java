package uk.gov.hmcts.reform.bailcaseapi.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Location;

@ExtendWith(MockitoExtension.class)
class StaffLocationTest {
    @ParameterizedTest
    @CsvSource({
        "Birmingham, 231596",
        "Glasgow, 366559",
        "Bradford, 698118",
        "Hatton Cross, 386417",
        "Manchester, 512401",
        "Newcastle, 366796",
        "Newport, 227101",
        "Taylor House, 765324",
        "Yarls Wood, 649000"
    })
    void should_get_correct_location_of_staff(String locationName, String epimsId) {
        Location result = StaffLocation.getLocation(HearingCentre.getHearingCentreByEpimsId(epimsId));
        assertEquals(locationName, result.getName());
    }
}
