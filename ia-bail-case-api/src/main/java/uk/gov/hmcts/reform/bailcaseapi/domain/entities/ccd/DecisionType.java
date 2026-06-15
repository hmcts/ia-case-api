package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

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

    public boolean isValidFor(Event event) {
        if (this == CONDITIONAL_GRANT) {
            return event == Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT;
        }
        return event == Event.UPLOAD_SIGNED_DECISION_NOTICE;
    }

    public static DecisionType getEnum(String value) {
        for (DecisionType v : values()) {
            if (v.id.equalsIgnoreCase(value)) {
                return v;
            }
        }
        return UNKNOWN;
    }
}
