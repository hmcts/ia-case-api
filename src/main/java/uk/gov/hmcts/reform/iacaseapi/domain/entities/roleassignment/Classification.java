package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Classification {
    PUBLIC, PRIVATE, RESTRICTED, @JsonEnumDefaultValue UNKNOWN
}
