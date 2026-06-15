package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NonLegalRepDetails {
    private String idamId;
    private String emailAddress;
    private String givenNames;
    private String familyName;
    @Setter
    private AddressUk addressUk;
    @Setter
    private String address;
    private String phoneNumber;

    @JsonIgnore
    public String getFullName() {
        return givenNames + " " + familyName;
    }
}

