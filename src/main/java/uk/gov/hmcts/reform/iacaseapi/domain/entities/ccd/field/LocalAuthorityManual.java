package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalAuthorityManual {

    @JsonProperty("localAuthorityName")
    private String localAuthorityName;

    @JsonProperty("localAuthorityAddress")
    private AddressGlobal localAuthorityAddress;

    @JsonProperty("localAuthorityEmail")
    private String localAuthorityEmail;

    public boolean isCorrectlyFilledIn() {

        return !StringUtils.isEmpty(localAuthorityName)
               && !StringUtils.isEmpty(localAuthorityEmail)
               && localAuthorityAddress.getAddressLine1().isPresent()
               && localAuthorityAddress.getPostTown().isPresent()
               && localAuthorityAddress.getCountry().isPresent()
               && localAuthorityAddress.getPostCode().isPresent();
    }

    public boolean isPartiallyFilledIn() {

        return !StringUtils.isEmpty(localAuthorityName)
               || !StringUtils.isEmpty(localAuthorityEmail)
               || localAuthorityAddress.getAddressLine1().isPresent()
               || localAuthorityAddress.getAddressLine2().isPresent()
               || localAuthorityAddress.getAddressLine3().isPresent()
               || localAuthorityAddress.getPostTown().isPresent()
               || localAuthorityAddress.getCounty().isPresent()
               || localAuthorityAddress.getCountry().isPresent()
               || localAuthorityAddress.getPostCode().isPresent();
    }

    public boolean isTotallyBlank() {
        return !isCorrectlyFilledIn()
               && localAuthorityAddress.getAddressLine2().isPresent()
               && localAuthorityAddress.getAddressLine3().isPresent()
               && localAuthorityAddress.getCounty().isPresent();
    }
}
