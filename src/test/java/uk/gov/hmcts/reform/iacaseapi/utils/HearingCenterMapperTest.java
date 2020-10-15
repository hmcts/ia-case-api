package uk.gov.hmcts.reform.iacaseapi.utils;

import static junit.framework.TestCase.assertEquals;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Location;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.casemanagementlocation.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.HearingCenterMapper;

@RunWith(JUnitParamsRunner.class)
public class HearingCenterMapperTest {

    @Test
    public void should_get_correct_location_of_staff_birmingham() {
        HearingCentre hearingCentre = HearingCentre.BIRMINGHAM;
        BaseLocation result = HearingCenterMapper.getBaseLocation(hearingCentre);

        //fixme:        assertEquals("Birmingham", result);
    }

    @Test
    public void should_get_correct_location_of_staff_glasgow() {
        HearingCentre hearingCentre = HearingCentre.GLASGOW;
        //        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
        //fixme:        assertEquals("Glasgow", result.getName());
    }

    @Test
    public void should_get_correct_location_of_staff_bradford() {
        HearingCentre hearingCentre = HearingCentre.BRADFORD;
        //fixme: Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
        //        assertEquals("Bradford", result.getName());
    }

    @Test
    public void should_get_correct_location_of_staff_hatton() {
        HearingCentre hearingCentre = HearingCentre.HATTON_CROSS;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Hatton Cross", result.getName());
    }

    @Test
    public void should_get_correct_location_of_staff_manchester() {
        HearingCentre hearingCentre = HearingCentre.MANCHESTER;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Manchester", result.getName());
    }

    @Test
    public void should_get_correct_location_of_staff_newcastle() {
        HearingCentre hearingCentre = HearingCentre.NEWCASTLE;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Newcastle", result.getName());
    }

    @Test
    public void should_get_correct_location_of_staff_newport() {
        HearingCentre hearingCentre = HearingCentre.NEWPORT;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Newport", result.getName());
    }

    @Test
    public void should_get_correct_location_of_staff_taylor() {
        HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Taylor House", result.getName());
    }

    @Test
    public void should_get_correct_location_of_Coventry_Magistrates_Court() {
        HearingCentre hearingCentre = HearingCentre.COVENTRY;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Birmingham", result.getName());
    }

    @Test
    public void should_get_correct_location_of_Nottingham() {
        HearingCentre hearingCentre = HearingCentre.NOTTINGHAM;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Birmingham", result.getName());
    }

    @Test
    public void should_get_correct_location_of_Glasgow() {
        HearingCentre hearingCentre = HearingCentre.GLASGOW_TRIBUNALS_CENTRE;
//        Location result = HearingCenterMapper.getBaseLocation(hearingCentre);
//        assertEquals("Glasgow", result.getName());
    }
}
