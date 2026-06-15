package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@AllArgsConstructor
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

    public String getFullName() {
        return givenNames + " " + familyName;
    }
}

