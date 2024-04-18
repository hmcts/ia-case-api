package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@ExtendWith(MockitoExtension.class)
public class LocationRefDataServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserDetails userDetails;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    CourtLocationCategory locationCategory;

    @Mock
    CourtVenue openHearingCourtVenue;

    @Mock
    CourtVenue closedHearingCourtVenue;

    @Mock
    CourtVenue openNonHearingCourtVenue;

    @Mock
    CourtVenue closedNonHearingCourtVenue;

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
    }

    @Test
    void should_return_dynamicList_when_getHearingLocationsDynamicList() {
        String token = "token";
        when(userDetails.getAccessToken()).thenReturn(token);
        String authToken = "authToken";
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(
            token,
            authToken,
            serviceId
        )).thenReturn(locationCategory);

        openHearingCourtVenue = new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "783803",
            "Y",
            "Open");

        closedHearingCourtVenue = new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "783803",
            "Y",
            "Closed");

        openNonHearingCourtVenue = new CourtVenue("Manchester Magistrates",
            "Birmingham Civil and Family Justice Centre",
            "231596",
            "N",
            "Open");

        closedNonHearingCourtVenue = new CourtVenue("Manchester Magistrates",
            "Birmingham Civil and Family Justice Centre",
            "231596",
            "N",
            "Closed");

        when(locationCategory.getCourtVenues()).thenReturn(List.of(
            openHearingCourtVenue,
            openNonHearingCourtVenue,
            closedHearingCourtVenue,
            closedNonHearingCourtVenue)
        );
        dynamicList = new DynamicList(new Value("", ""),
            List.of(new Value(openHearingCourtVenue.getEpimmsId(),
                openHearingCourtVenue.getCourtName())));

        assertEquals(dynamicList, locationRefDataService.getHearingLocationsDynamicList());
    }
}

