package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class CaseManagementLocationServiceTest {

    @Mock
    LocationRefDataService locationRefDataService;
    private CaseManagementLocationService service;

    @BeforeEach
    public void setUp() {
        service = new CaseManagementLocationService(locationRefDataService);
    }

    @ParameterizedTest
    @CsvSource({
        "Birmingham, BIRMINGHAM",
        "Glasgow, GLASGOW",
        "Bradford, BRADFORD",
        "Hatton Cross, HATTON_CROSS",
        "Manchester, MANCHESTER",
        "Newport, NEWPORT",
        "Taylor House, TAYLOR_HOUSE",
        "Newcastle, NEWCASTLE",
        "North Shields, NORTH_SHIELDS",
        "IAC National (Virtual), IAC_NATIONAL_VIRTUAL"
    })
    void given_staffLocationName_then_return_caseManagementLocation(
        String staffLocationName, BaseLocation baseLocation) {

        CaseManagementLocation actual = service.getCaseManagementLocation(staffLocationName);

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation()).isEqualTo(baseLocation);
    }

    @Test
    void getRefDataCaseManagementLocation() {
        Value courtVenue = new Value(HearingCentre.TAYLOR_HOUSE.getEpimsId(), "Taylor House Ref Data");
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData actual = service.getRefDataCaseManagementLocation("Taylor House");

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation()).isEqualTo(cmlDynamicList);
    }

    @Test
    void getRefDataCaseManagementLocation_returnDefaultLocationNewportIfNotFound() {
        Value courtVenue = new Value(HearingCentre.NEWPORT.getEpimsId(), "Newport Ref Data");
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData actual = service.getRefDataCaseManagementLocation("Newcastle");

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation().getValue().getLabel()).isEqualTo("Newport Ref Data");
    }


}
