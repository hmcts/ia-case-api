package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class LegacyCaseFlag {

    @NonNull
    CaseFlagType legacyCaseFlagType;
    @NonNull
    String legacyCaseFlagAdditionalInformation;

}
