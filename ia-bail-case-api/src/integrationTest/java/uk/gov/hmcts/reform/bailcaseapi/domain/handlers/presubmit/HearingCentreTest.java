package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingCentreFinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingCentreTest extends SpringBootIntegrationTest {

    @Autowired private HearingCentreFinder hearingCentreFinder;

    @Test
    void should_derive_hearing_centres() {

        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Ashwell"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Aylesbury"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Bedford"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Birmingham"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Blakenhurst"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Brinsford"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Brockhill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Bullingdon"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Eastwood Park"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Featherstone"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Foston Hall"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Gartree"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Glen Parva"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Gloucester"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Grendon/Spring Hill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Hewell Grange"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Leicester"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Leyhill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Lincoln"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Littlehey"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Long Lartin"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Lowdham Grange"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Morton Hall"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("North Sea Camp"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Nottingham"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Oakwood"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Onley"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Peterborough"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Reading"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Rye Hill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Shrewsbury"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Spring Hill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Stafford"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Stocken"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Stoke Heath"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Sudbury"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Swinfen Hall"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Wellingborough"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Whatton"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Whitemoor"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Woodhill"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Five Wells"));
        assertEquals(HearingCentre.BIRMINGHAM, hearingCentreFinder.find("Fosse Way"));

        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Acklington"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Askham Grange"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Castington"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Deerbolt"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Derwentside"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Doncaster"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Durham"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Everthorpe"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Frankland"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Full Sutton"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Hatfield"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Holme House"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Hull"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Humber"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Kirklevington Grange"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Leeds"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Lindholme"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Low Newton"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Moorland Closed"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Moorland Open"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("New Hall"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Northallerton"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Ranby"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Wakefield"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Wealstun"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Wetherby"));
        assertEquals(HearingCentre.BRADFORD, hearingCentreFinder.find("Wolds"));

        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Addiewell"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Barlinnie"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Castle Huntly"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Cornton Vale"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Dumfries"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Dungavel"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Edinburgh, Saughton"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Glenochil"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Grampian"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Greenock"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Hydebank Wood"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Inverness"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Kilmarnock"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Larne House"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Low Moss"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Magilligan"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Maghaberry"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Perth"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Polmont"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Shotts"));
        assertEquals(HearingCentre.GLASGOW, hearingCentreFinder.find("Stirling"));

        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Albany"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Brixton"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Bronzefield"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Camp Hill"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Coldingley"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Colnbrook"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Downview"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Feltham"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Harmondsworth"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("High Down"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Holloway"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Huntercombe"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Kingston"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Isle of Wight"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Latchmere House"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Lewes"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Parkhurst"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Send"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("The Mount"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Wandsworth"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Winchester"));
        assertEquals(HearingCentre.HATTON_CROSS, hearingCentreFinder.find("Wormwood Scrubs"));

        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Altcourse"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Berwyn"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Buckley Hall"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Dovegate"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Drake Hall"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Forest Bank"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Garth"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Haverigg"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Hindley"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Kennet"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Kirkham"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Lancaster"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Lancaster Farms"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Liverpool"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Manchester"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Preston"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Risley"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Styal"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Thorn Cross"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Werrington"));
        assertEquals(HearingCentre.MANCHESTER, hearingCentreFinder.find("Wymott"));

        assertEquals(HearingCentre.NEWCASTLE, hearingCentreFinder.find("Northumberland"));

        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Ashfield"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Bristol"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Campsfield House"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Cardiff"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Channings Wood"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Dartmoor"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Dorchester"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Erlestoke"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Exeter"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Guys Marsh"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Parc"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Portland"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Prescoed"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Shepton Mallet"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Swansea"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("The Weare"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("The Verne"));
        assertEquals(HearingCentre.NEWPORT, hearingCentreFinder.find("Usk"));

        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Belmarsh"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Blantyre House"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Blundeston"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Brookhouse"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Bullwood Hall"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Bure"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Canterbury"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Chelmsford"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Cookham Wood"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("East Sutton Park"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Edmunds Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Elmley"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Ford"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Highpoint"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Hollesley Bay/Hmyoi Warren Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Isis"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Maidstone"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Norwich"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Pentonville"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Rochester"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Standford Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Swaleside"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Thameside"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Tinsley House"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Warren Hill"));
        assertEquals(HearingCentre.TAYLOR_HOUSE, hearingCentreFinder.find("Wayland"));

        assertEquals(HearingCentre.YARLS_WOOD, hearingCentreFinder.find("Yarlswood"));

    }
}
