package uk.gov.hmcts.reform.iacaseapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FeeType {

    FEE_WITH_HEARING("feeWithHearing"),
    FEE_WITHOUT_HEARING("feeWithoutHearing");

    @JsonValue
    private final String value;

    FeeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
