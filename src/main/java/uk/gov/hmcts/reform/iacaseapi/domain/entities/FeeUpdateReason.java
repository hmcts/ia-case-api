package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FeeUpdateReason {

    DECISION_TYPE_CHANGED("decisionTypeChanged"),
    APPEAL_NOT_VALID("appealNotValid"),
    FEE_REMISSION_CHANGED("feeRemissionChanged");

    @JsonValue
    private final String value;

    FeeUpdateReason(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
