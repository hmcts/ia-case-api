package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.LegRepAddressUk;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ProfessionalUser;

import java.util.List;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationEntityResponse {

    private String organisationIdentifier;
    private String name;
    private String status;
    private boolean sraRegulated;
    private ProfessionalUser superUser;
    private List<String> paymentAccount;
    private List<LegRepAddressUk> contactInformation;

    public String getOrganisationIdentifier() {
        requireNonNull(organisationIdentifier);
        return organisationIdentifier;
    }

    public List<String> getPaymentAccount() {
        return paymentAccount;
    }

    public List<LegRepAddressUk> getContactInformation() {
        return contactInformation;
    }
}
