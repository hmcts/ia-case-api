package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                                               "Y",
                                               "Open",
                                               "The Court House, Minshull Street",
                                               "M1 3FS");

    private CourtVenue closedHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                                 "Manchester Magistrates Court",
                                                 "783803",
                                                 "Y",
                                                 "Y",
                                                 "Closed",
                                                 "The Court House, Minshull Street",
                                                 "M1 3FS");

    private CourtVenue openNonHearingCourtVenue = new CourtVenue("Birmingham Civil and Family Justice Centre",
                                                  "Birmingham Civil and Family Justice Centre",
                                                  "231596",
                                                  "N",
                                                  "N",
                                                  "Open",
                                                  "Priory Courts, 33 Bull Street",
                                                  "B4 6DS");

    private CourtVenue closedNonHearingCourtVenue = new CourtVenue("Birmingham Civil and Family Justice Centre",
                                                    "Birmingham Civil and Family Justice Centre",
                                                    "231596",
                                                    "N",
                                                    "N",
                                                    "Closed",
                                                    "Priory Courts, 33 Bull Street",
                                                    "B4 6DS");

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
            openNonHearingCourtVenue,
            closedHearingCourtVenue,
            closedNonHearingCourtVenue)
        );
    }

    @Test
    void should_return_dynamicList_when_getHearingLocationsDynamicList() {

        dynamicList = new DynamicList(new Value("", ""),
            List.of(new Value(openHearingCourtVenue.getEpimmsId(),
                openHearingCourtVenue.getCourtName())));

        assertEquals(dynamicList, locationRefDataService.getHearingLocationsDynamicList());
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

