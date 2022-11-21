package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.NonNull;
import lombok.Value;

@Value
public class LegacyCaseFlag {

    @NonNull
    CaseFlagType legacyCaseFlagType;
    @NonNull
    String legacyCaseFlagAdditionalInformation;

}
