package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.REMOTE_HEARING;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private static final String OPEN = "Open";
    private static final String Y = "Y";

    private final AuthTokenGenerator authTokenGenerator;
    private final UserDetails userDetails;
    private final LocationRefDataApi locationRefDataApi;
    @org.springframework.beans.factory.annotation.Value("${hmcts_service_id}")
    private String serviceId;

    public DynamicList getHearingLocationsDynamicList() {

        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .filter(this::isOpenHearingLocation)
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    public List<CourtVenue> getCourtVenues() {

        CourtLocationCategory locationCategory = locationRefDataApi
            .getCourtVenues(userDetails.getAccessToken(), authTokenGenerator.generate(), serviceId);

        return locationCategory == null
            ? Collections.emptyList()
            : locationCategory.getCourtVenues();
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    private boolean isOpenHearingLocation(CourtVenue courtVenue) {
        return StringUtils.equals(courtVenue.getCourtStatus(), OPEN)
               && StringUtils.equals(courtVenue.getIsHearingLocation(), Y);
    }

    public String getHearingCentreAddress(HearingCentre hearingCentre) {
        if (Objects.equals(hearingCentre, REMOTE_HEARING)) {
            return "Remote hearing";
        }
        return getCourtVenues()
            .stream()
            .filter(courtVenue -> StringUtils.equals(courtVenue.getEpimmsId(), hearingCentre.getEpimsId()))
            .findFirst()
            .map(this::assembleCourtVenueAddress)
            .orElse("");
    }

    private String assembleCourtVenueAddress(CourtVenue courtVenue) {
        return defaultIfNull(courtVenue.getCourtName(), "") + ", "
               + defaultIfNull(courtVenue.getCourtAddress(), "") + ", "
               + defaultIfNull(courtVenue.getPostcode(), "");
    }
}
