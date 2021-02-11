package uk.gov.hmcts.reform.iacaseapi.domain.entities.ref;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationResponse {

    private OrganisationEntityResponse organisationEntityResponse;

    public OrganisationResponse(OrganisationEntityResponse organisationEntityResponse) {
        this.organisationEntityResponse = organisationEntityResponse;
    }

    public OrganisationResponse() {
    }
}
