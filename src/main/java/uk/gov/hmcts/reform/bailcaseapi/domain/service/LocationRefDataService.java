package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private final AuthTokenGenerator authTokenGenerator;
    private final UserDetails userDetails;
    private final LocationRefDataApi locationRefDataApi;
    @org.springframework.beans.factory.annotation.Value("${hmcts_service_id}")
    private String serviceId;

    public DynamicList getHearingLocationsDynamicList() {

        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .filter(this::isHearingLocation)
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    public DynamicList getCaseManagementLocationsDynamicList() {

        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .filter(this::isCaseManagementLocation)
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    public Optional<CourtVenue> getCourtVenuesByEpimmsId(String epimmsId) {
        return getCourtVenues().stream()
            .filter(this::isHearingLocation)
            .filter(courtVenue -> courtVenue.getEpimmsId().equals(epimmsId))
            .findFirst();
    }

    private List<CourtVenue> getCourtVenues() {

        CourtLocationCategory locationCategory = locationRefDataApi
            .getCourtVenues(userDetails.getAccessToken(), authTokenGenerator.generate(), serviceId);

        return locationCategory == null
            ? Collections.emptyList()
            : locationCategory.getCourtVenues();
    }

    private boolean isHearingLocation(CourtVenue courtVenue) {

        return Objects.equals(courtVenue.getIsHearingLocation(), "Y")
               && Objects.equals(courtVenue.getCourtStatus(), "Open");
    }

    private boolean isCaseManagementLocation(CourtVenue courtVenue) {

        return Objects.equals(courtVenue.getIsCaseManagementLocation(), "Y")
               && Objects.equals(courtVenue.getCourtStatus(), "Open");
    }
}
