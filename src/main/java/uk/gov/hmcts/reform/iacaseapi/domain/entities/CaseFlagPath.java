package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class CaseFlagPath {
    private String id;
    private String value;
}
