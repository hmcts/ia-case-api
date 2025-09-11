package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.REMOTE_HEARING;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
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
    private static final String COURT = "COURT";

    private final AuthTokenGenerator authTokenGenerator;
    private final UserDetails userDetails;
    private final LocationRefDataApi locationRefDataApi;
    @org.springframework.beans.factory.annotation.Value("${hmcts_service_id}")
    private String serviceId;

    public DynamicList getHearingLocationsDynamicList() {

        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .filter(courtVenue -> isOpenLocation(courtVenue) && isHearingLocation(courtVenue))
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    public DynamicList getCaseManagementLocationDynamicList() {
        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .filter(courtVenue -> isOpenLocation(courtVenue)
                && isCaseManagementLocation(courtVenue)
                && isCourtLocation(courtVenue))
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

    public boolean isCaseManagementLocation(String epimsId) {

        return getCourtVenues()
            .stream()
            .filter(this::isCaseManagementLocation)
            .anyMatch(courtVenue -> Objects.equals(courtVenue.getEpimmsId(), epimsId));
    }

    private boolean isCaseManagementLocation(CourtVenue courtVenue) {
        return Objects.equals(courtVenue.getIsCaseManagementLocation(), Y);
    }

    private boolean isCourtLocation(CourtVenue courtVenue) {
        return COURT.equals(courtVenue.getLocationType());
    }

    private boolean isOpenLocation(CourtVenue courtVenue) {
        return OPEN.equalsIgnoreCase(courtVenue.getCourtStatus());
    }

    private boolean isHearingLocation(CourtVenue courtVenue) {
        return Objects.equals(courtVenue.getIsHearingLocation(), Y);
    }

    public String getHearingCentreAddress(HearingCentre hearingCentre) {
        if (Objects.equals(hearingCentre, REMOTE_HEARING)
                && hearingCentre != HearingCentre.IAC_NATIONAL_VIRTUAL) {
            return "Remote hearing";
        }

        return getHearingCentreAddress(hearingCentre.getEpimsId());
    }

    public String getHearingCentreAddress(String epimsId) {

        return getCourtVenues()
            .stream()
            .filter(courtVenue -> Objects.equals(courtVenue.getEpimmsId(), epimsId))
            .findFirst()
            .map(this::assembleCourtVenueAddress)
            .orElse("");
    }

    private String assembleCourtVenueAddress(CourtVenue courtVenue) {
        return getIfNull(courtVenue.getCourtName(), "") + ", "
               + getIfNull(courtVenue.getCourtAddress(), "") + ", "
               + getIfNull(courtVenue.getPostcode(), "");
    }

}
