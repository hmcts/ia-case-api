package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;

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
    @EnumSource(HearingCentre.class)
    void given_staffLocationName_then_return_caseManagementLocation(HearingCentre hearingCentre) {
        CaseManagementLocation actual = service.getCaseManagementLocation(hearingCentre);

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation()).isEqualTo(BaseLocation.valueOf(hearingCentre.name()));
    }

    @Test
    void getRefDataCaseManagementLocation() {
        Value courtVenue = new Value(HearingCentre.TAYLOR_HOUSE.getEpimsId(), "Taylor House Ref Data");
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData actual = service.getRefDataCaseManagementLocation(HearingCentre.TAYLOR_HOUSE);

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation()).isEqualTo(cmlDynamicList);
    }

    @Test
    void getRefDataCaseManagementLocation_returnDefaultLocationNewportIfNotFound() {
        Value courtVenue = new Value(HearingCentre.NEWPORT.getEpimsId(), "Newport Ref Data");
        DynamicList cmlDynamicList = new DynamicList(courtVenue, List.of(courtVenue));
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(cmlDynamicList);

        CaseManagementLocationRefData actual = service.getRefDataCaseManagementLocation(HearingCentre.NEWCASTLE);

        assertThat(actual.getRegion()).isEqualTo(Region.NATIONAL);
        assertThat(actual.getBaseLocation().getValue().getLabel()).isEqualTo("Newport Ref Data");
    }
}
