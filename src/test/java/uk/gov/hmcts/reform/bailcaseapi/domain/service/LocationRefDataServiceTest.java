package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LocationRefDataServiceTest {

    @Mock
    private LocationRefDataApi locationRefDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserDetails userDetails;

    private LocationRefDataService locationRefDataService;

    @BeforeEach
    void setup() {
        List<CourtVenue> courtVenues = List.of(
            CourtVenue.builder()
                .epimmsId("1234")
                .courtName("Manchester")
                .courtStatus("Open")
                .isHearingLocation("Y")
                .isCaseManagementLocation("N")
                .courtAddress("Crown Square, Manchester, Greater Manchester")
                .postcode("M60 1PR")
                .build(),
            CourtVenue.builder()
                .epimmsId("3344")
                .courtName("Birmingham")
                .courtStatus("Open")
                .isHearingLocation("N")
                .isCaseManagementLocation("Y")
                .courtAddress("Priory Courts, 33 Bull Street")
                .postcode("B4 6DS")
                .build(),
            CourtVenue.builder()
                .epimmsId("1234")
                .courtName("Bradford")
                .courtStatus("Closed")
                .isCaseManagementLocation("N")
                .isHearingLocation("N")
                .courtAddress("The Tyrls, PO Box 187")
                .postcode("BD1 1JL")
                .build()
        );
        CourtLocationCategory courtLocationCategory = CourtLocationCategory
            .builder().courtTypeId("111").courtType("IAC").serviceCode("BFA1").courtVenues(courtVenues).build();
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(userDetails.getAccessToken()).thenReturn("userAccessToken");
        when(locationRefDataApi.getCourtVenues(any(), any(), any()))
            .thenReturn(courtLocationCategory);

        locationRefDataService = new LocationRefDataService(authTokenGenerator, userDetails, locationRefDataApi);
    }

    @Test
    void test_getHearingLocationsDynamicList() {

        DynamicList dynamicList = locationRefDataService.getHearingLocationsDynamicList();

        assertEquals(1, dynamicList.getListItems().size());
        assertEquals("Manchester", dynamicList.getListItems().get(0).getLabel());
        assertEquals("1234", dynamicList.getListItems().get(0).getCode());
    }

    @Test
    void test_getCaseManagementLocationsDynamicList() {

        DynamicList dynamicList = locationRefDataService.getCaseManagementLocationsDynamicList();

        assertEquals(1, dynamicList.getListItems().size());
        assertEquals("Birmingham", dynamicList.getListItems().get(0).getLabel());
        assertEquals("3344", dynamicList.getListItems().get(0).getCode());
    }

    @Test
    void test_getCourtVenuesByEpimmsId() {

        Optional<CourtVenue> courtVenue  = locationRefDataService.getCourtVenuesByEpimmsId("1234");

        assertEquals(true, courtVenue.isPresent());
        assertEquals("Crown Square, Manchester, Greater Manchester", courtVenue.get().getCourtAddress());
        assertEquals("M60 1PR", courtVenue.get().getPostcode());
    }

}
