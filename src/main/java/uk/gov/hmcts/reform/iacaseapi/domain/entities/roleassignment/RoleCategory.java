package uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum RoleCategory {
    JUDICIAL,  STAFF, LEGAL_OPERATIONS, PROFESSIONAL, CITIZEN, @JsonEnumDefaultValue UNKNOWN
}
