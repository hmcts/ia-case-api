package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DecisionType {

    GRANTED("granted"),
    REFUSED("refused"),
    CONDITIONAL_GRANT("conditionalGrant"),
    REFUSED_UNDER_IMA("refusedUnderIma"),
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    DecisionType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
