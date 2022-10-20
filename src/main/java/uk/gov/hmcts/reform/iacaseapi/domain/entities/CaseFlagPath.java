package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@AllArgsConstructor
@Value
@EqualsAndHashCode
public class CaseFlagPath {
    private String id;
    private String value;
}
