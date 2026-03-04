package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@AllArgsConstructor
public class NonLegalRepDetails {
    @NonNull private String idamId;
    @NonNull private String emailAddress;
    @NonNull private String givenNames;
    @NonNull private String familyName;
    private String phoneNumber;
}
