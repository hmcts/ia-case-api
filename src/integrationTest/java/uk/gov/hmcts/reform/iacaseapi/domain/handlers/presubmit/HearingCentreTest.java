package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
public class HearingCentreTest {

    @Autowired private HearingCentreFinder hearingCentreFinder;

    @Test
    public void should_derive_hearing_centres() {

        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("BD1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("DN1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HD1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HG1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HU1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HX1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("LS1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("S1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("WF1 1AB"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("YO1 1AB"));

        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("L1 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("LA1 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("M1 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("OL2 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("PR2 3AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("SK3 4AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("WA4 5AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("WN5 6AB"));

        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("BA1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("BS1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("CF1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("DT1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("EX1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("HR1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("LD1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("NP1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("PL1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("SA1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("SN1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("SP1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("TA1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("TQ1 1AB"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("TR1 1AB"));

        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("BN1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("CB1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("CM1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("HP1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("IP1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("ME1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("N1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("NR1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("RH1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("SE1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("TN1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("W1 1AA"));

        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("XX"));
    }
}
