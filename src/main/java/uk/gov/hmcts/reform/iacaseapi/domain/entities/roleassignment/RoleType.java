package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum RoleType {
    CASE, ORGANISATION, @JsonEnumDefaultValue UNKNOWN
}
