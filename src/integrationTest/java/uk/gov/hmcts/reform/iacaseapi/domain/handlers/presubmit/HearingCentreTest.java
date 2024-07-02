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
    void should_derive_hearing_centres_from_postcode() {

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

    @Test
    void should_derive_hearing_centres_from_detention_centre_name() {
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Ashwell"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Aylesbury"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Bedford"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Birmingham"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Blakenhurst"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Brinsford"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Brockhill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Bullingdon"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Eastwood Park"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Featherstone"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Five Wells"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Fosse Way"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Foston Hall"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Gartree"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Glen Parva"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Gloucester"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Grendon/Spring Hill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Hewell Grange"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Leicester"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Leyhill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Lincoln"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Littlehey"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Long Lartin"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Lowdham Grange"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Morton Hall"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("North Sea Camp"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Nottingham"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Onley"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Peterborough"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Reading"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Rye Hill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Shrewsbury"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Spring Hill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Stafford"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Stocken"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Stoke Heath"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Sudbury"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Swinfen Hall"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Wellingborough"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Whatton"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Whitemoor"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Woodhill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Five Wells"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.findByDetentionFacility("Fosse Way"));

        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Acklington"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Askham Grange"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Castington"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Deerbolt"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Derwentside"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Doncaster"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Durham"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Everthorpe"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Frankland"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Full Sutton"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Holme House"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Hull"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Kirklevington Grange"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Leeds"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Lindholme"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Low Newton"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Moorland Closed"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Moorland Open"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("New Hall"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Northallerton"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Ranby"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Wakefield"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Wealstun"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Wetherby"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.findByDetentionFacility("Wolds"));

        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Addiewell"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Barlinnie"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Castle Huntly"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Cornton Vale"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Dumfries"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Dungavel"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Edinburgh, Saughton"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Glenochil"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Grampian"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Greenock"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Hydebank Wood"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Inverness"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Kilmarnock"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Low Moss"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Magilligan"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Maghaberry"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Perth"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Polmont"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.findByDetentionFacility("Shotts"));

        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Albany"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Brixton"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Bronzefield"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Camp Hill"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Coldingley"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Colnbrook"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Downview"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Feltham"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Harmondsworth"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("High Down"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Holloway"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Huntercombe"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Kingston"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Latchmere House"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Lewes"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Parkhurst"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Send"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("The Mount"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Wandsworth"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Winchester"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.findByDetentionFacility("Wormwood Scrubs"));

        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Altcourse"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Buckley Hall"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Dovegate"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Drake Hall"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Forest Bank"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Garth"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Haverigg"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Hindley"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Kennet"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Kirkham"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Lancaster"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Lancaster Farms"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Liverpool"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Manchester"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Preston"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Risley"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Styal"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Thorn Cross"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Werrington"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.findByDetentionFacility("Wymott"));

        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Ashfield"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Bristol"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Cardiff"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Channings Wood"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Dartmoor"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Dorchester"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Erlestoke"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Exeter"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Guys Marsh"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Parc"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Portland"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Prescoed"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Shepton Mallet"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Swansea"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("The Weare"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.findByDetentionFacility("Usk"));

        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Belmarsh"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Blantyre House"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Blundeston"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Brookhouse"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Bullwood Hall"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Canterbury"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Chelmsford"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Cookham Wood"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("East Sutton Park"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Edmunds Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Elmley"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Ford"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Highpoint"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Hollesley Bay/Hmyoi Warren Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Isis"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Maidstone"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Norwich"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Pentonville"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Rochester"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Standford Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Swaleside"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Thameside"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Tinsley House"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Warren Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.findByDetentionFacility("Wayland"));

        assertEquals(HearingCentre.YARLSWOOD, hearingCentreFinder.findByDetentionFacility("Yarlswood"));

        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Berwyn"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Bure"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Hatfield"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Humber"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Isle of Wight"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Northumberland"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("Oakwood"));
        assertEquals(HearingCentre.UNKNOWN, hearingCentreFinder.findByDetentionFacility("The Verne"));
    }
}
