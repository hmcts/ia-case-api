package uk.gov.hmcts.reform.iacaseapi.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Location;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@ExtendWith(MockitoExtension.class)
class StaffLocationTest {

    @Test
    void should_get_correct_location_of_staff_birmingham() {
        HearingCentre hearingCentre = HearingCentre.BIRMINGHAM;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Birmingham", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_glasgow() {
        HearingCentre hearingCentre = HearingCentre.GLASGOW;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Glasgow", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_bradford() {
        HearingCentre hearingCentre = HearingCentre.BRADFORD;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Bradford", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_hatton() {
        HearingCentre hearingCentre = HearingCentre.HATTON_CROSS;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Hatton Cross", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_manchester() {
        HearingCentre hearingCentre = HearingCentre.MANCHESTER;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Manchester", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_newcastle() {
        HearingCentre hearingCentre = HearingCentre.NEWCASTLE;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Newcastle", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_newport() {
        HearingCentre hearingCentre = HearingCentre.NEWPORT;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Newport", result.getName());
    }

    @Test
    void should_get_correct_location_of_staff_taylor() {
        HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Taylor House", result.getName());
    }

    @Test
    void should_get_correct_location_of_Coventry_Magistrates_Court() {
        HearingCentre hearingCentre = HearingCentre.COVENTRY;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Birmingham", result.getName());
    }

    @Test
    void should_get_correct_location_of_Nottingham() {
        HearingCentre hearingCentre = HearingCentre.NOTTINGHAM;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Birmingham", result.getName());
    }

    @Test
    void should_get_correct_location_of_Glasgow() {
        HearingCentre hearingCentre = HearingCentre.GLASGOW_TRIBUNALS_CENTRE;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Glasgow", result.getName());
    }

    @Test
    void should_get_correct_location_of_Harmondsworth() {
        HearingCentre hearingCentre = HearingCentre.HARMONDSWORTH;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Harmondsworth", result.getName());
    }

    @Test
    void should_get_correct_location_of_Yarlswood() {
        HearingCentre hearingCentre = HearingCentre.YARLS_WOOD;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Yarls Wood", result.getName());
    }

    @Test
    public void should_get_correct_location_of_North_Shields() {
        HearingCentre hearingCentre = HearingCentre.NORTH_SHIELDS;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("North Shields", result.getName());
    }

    @Test
    void should_get_correct_location_of_Remote_Hearing() {
        HearingCentre hearingCentre = HearingCentre.REMOTE_HEARING;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Remote hearing", result.getName());
    }

    @Test
    public void should_get_correct_location_of_Decision_Without_Hearing() {
        HearingCentre hearingCentre = HearingCentre.DECISION_WITHOUT_HEARING;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("Decision Without Hearing", result.getName());
    }

    @Test
    void should_get_correct_location_of_Virtual_Hearing() {
        HearingCentre hearingCentre = HearingCentre.IAC_NATIONAL_VIRTUAL;
        Location result = StaffLocation.getLocation(hearingCentre);
        assertEquals("IAC National (Virtual)", result.getName());
    }

    @ParameterizedTest
    @CsvSource({
        "Birmingham, 231596",
        "Birmingham, 618632",
        "Birmingham, 787030",
        "Glasgow, 366559",
        "Glasgow, 999973",
        "Bradford, 698118",
        "Hatton Cross, 386417",
        "Manchester, 512401",
        "Newcastle, 366796",
        "Newport, 227101",
        "Taylor House, 765324",
        "IAC National (Virtual), 999970"
    })
    void should_get_correct_location_of_staff(String locationName, String epimsId) {
        Location result = StaffLocation.getLocation(epimsId);
        assertEquals(locationName, result.getName());
    }
}
