package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FeeTribunalAction {

    REFUND("refund"),
    ADDITIONAL_PAYMENT("additionalPayment"),
    NO_ACTION("noAction");
    @JsonValue
    private final String value;

    FeeTribunalAction(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
