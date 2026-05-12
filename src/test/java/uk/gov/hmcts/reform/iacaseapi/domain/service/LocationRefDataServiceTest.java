package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.BIRMINGHAM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.REMOTE_HEARING;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LocationRefDataServiceTest {

    private static final String COURT = "COURT";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserDetails userDetails;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    private CourtLocationCategory locationCategory;

    private CourtVenue openHearingCourtVenue = new CourtVenue("Manchester Magistrates",
        "Manchester Magistrates Court",
        "783803",
        "Y",
        "N",
        "Open",
        "The Court House, Minshull Street",
        "M1 3FS",
        COURT);

    private CourtVenue openVrHearingCourtVenue = new CourtVenue("IAC National (Virtual)",
        "IAC National (Virtual)",
        "999970",
        "Y",
        "N",
        "OPEN",
        "Piccadilly Plaza",
        "M1 4AH",
        COURT);

    private CourtVenue openCaseManagementLocation = new CourtVenue(
        "Newcastle Civil & Family Courts and Tribunals Centre",
        "Newcastle Civil And Family Courts And Tribunals Centre",
        "366796",
        "N",
        "Y",
        "Open",
        "Barras Bridge, Newcastle-Upon-Tyne",
        "NE1 8QF",
        COURT);

    private CourtVenue nonCourtLocation = new CourtVenue(
        "Newcastle Civil & Family Courts and Tribunals Centre",
        "Newcastle Civil And Family Courts And Tribunals Centre",
        "366796",
        "N",
        "Y",
        "Open",
        "Barras Bridge, Newcastle-Upon-Tyne",
        "NE1 8QF",
        "non court");

    private CourtVenue closedHearingCourtVenue = new CourtVenue("Manchester Magistrates",
        "Manchester Magistrates Court",
        "783803",
        "Y",
        "N",
        "Closed",
        "The Court House, Minshull Street",
        "M1 3FS",
        COURT);

    private CourtVenue openNonHearingCourtVenue = new CourtVenue("Birmingham Civil and Family Justice Centre",
        "Birmingham Civil and Family Justice Centre",
        "231596",
        "N",
        "N",
        "Open",
        "Priory Courts, 33 Bull Street",
        "B4 6DS",
        COURT);

    private CourtVenue closedNonHearingCourtVenue = new CourtVenue("Birmingham Civil and Family Justice Centre",
        "Birmingham Civil and Family Justice Centre",
        "231596",
        "N",
        "N",
        "Closed",
        "Priory Courts, 33 Bull Street",
        "B4 6DS",
        COURT);

    @Mock
    DynamicList dynamicList;

    private LocationRefDataService locationRefDataService;

    private final String serviceId = "BFA1";

    @BeforeEach
    void setup() {
        locationRefDataService = new LocationRefDataService(
            authTokenGenerator,
            userDetails,
            locationRefDataApi
        );
        locationRefDataService.setServiceId(serviceId);

        String token = "token";
        when(userDetails.getAccessToken()).thenReturn(token);
        String authToken = "authToken";
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(
            token,
            authToken,
            serviceId
        )).thenReturn(locationCategory);

        when(locationCategory.getCourtVenues()).thenReturn(List.of(
            openHearingCourtVenue,
            openVrHearingCourtVenue,
            openNonHearingCourtVenue,
            closedHearingCourtVenue,
            closedNonHearingCourtVenue,
            openCaseManagementLocation)
        );
    }

    @Test
    void should_return_dynamicList_when_getHearingLocationsDynamicList() {

        dynamicList = new DynamicList(new Value("", ""),
            List.of(new Value(openHearingCourtVenue.getEpimmsId(), openHearingCourtVenue.getCourtName()),
                new Value(openVrHearingCourtVenue.getEpimmsId(), openVrHearingCourtVenue.getCourtName())));

        assertEquals(dynamicList, locationRefDataService.getHearingLocationsDynamicList());
    }

    @Test
    void should_return_dynamicList_when_getCaseManagementLocationDynamicList() {
        when(locationCategory.getCourtVenues()).thenReturn(List.of(
            openCaseManagementLocation,
            nonCourtLocation));

        dynamicList = new DynamicList(new Value("", ""),
            List.of(new Value(openCaseManagementLocation.getEpimmsId(),
                openCaseManagementLocation.getCourtName())));

        assertEquals(dynamicList, locationRefDataService.getCaseManagementLocationDynamicList());
    }

    @Test
    void isCaseManagementLocation_should_return_true() {

        assertTrue(locationRefDataService
            .isCaseManagementLocation(openCaseManagementLocation.getEpimmsId()));
    }

    @Test
    void isCaseManagementLocation_should_return_false() {

        assertFalse(locationRefDataService
            .isCaseManagementLocation(openHearingCourtVenue.getEpimmsId()));
    }

    @Test
    void should_get_address_given_non_remote_hearing_centre() {
        String expectedAddress = "Birmingham Civil and Family Justice Centre, Priory Courts, 33 Bull Street, B4 6DS";
        assertEquals(expectedAddress, locationRefDataService.getHearingCentreAddress(BIRMINGHAM));
    }

    @Test
    void should_not_get_address_given_remote_hearing_centre() {
        String expectedAddress = "Remote hearing";
        assertEquals(expectedAddress, locationRefDataService.getHearingCentreAddress(REMOTE_HEARING));
    }
}

