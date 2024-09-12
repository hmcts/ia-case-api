package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

class HearingCentreTest extends SpringBootIntegrationTest {

    @Autowired private HearingCentreFinder hearingCentreFinder;

    @Test
    void should_derive_hearing_centres() {

        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("BD1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("DN1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HD1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HG1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HU1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("HX1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("LS1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("S1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("WF1 1AA"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("YO1 1AA"));

        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("L1 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("LA1 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("M1 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("OL2 2AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("PR2 3AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("SK3 4AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("WA4 5AB"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("WN5 6AB"));

        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("BA1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("BS1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("CF1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("DT1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("EX1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("HR1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("LD1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("NP1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("PL1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("SA1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("SN1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("SP1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("TA1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("TQ1 1AA"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("TR1 1AA"));

        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("BN1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("CB1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("CM1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("IP1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("ME1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("N1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("NR1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("RH1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("SE1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("TN1 1AA"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("W1 1AA"));

        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("CA1 1AA"));
        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("DH1 1AA"));
        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("DL1 1AA"));
        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("NE1 1AA"));
        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("SR1 1AA"));
        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("TS1 1AA"));

        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("B1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("CV1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("DE1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("DY1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("GL1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("HP1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("LE1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("LN1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("LU1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("MK1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("NG1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("NN1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("OX1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("PE1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("RG1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("SY1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("TF1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("WD1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("WR1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("WS1 1AA"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("WV1 1AA"));

        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("BH1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("GU1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("HA1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("KT1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("PO1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("SL1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("SM1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("SO1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("SW1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("TW1 1AA"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("UB1 1AA"));

        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("AB1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("DD1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("DG1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("EH1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("FK1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("G1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("HS1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("IV1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("KA1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("KW1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("KY1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("ML1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("PA1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("PH1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("TD1 1AA"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("ZE1 1AA"));

        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("XX"));
    }
}
