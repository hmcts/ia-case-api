package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.*;
import lombok.Value;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegacyCaseFlag {

    @NonNull
    CaseFlagType legacyCaseFlagType;
    @NonNull
    String legacyCaseFlagAdditionalInformation;

}
