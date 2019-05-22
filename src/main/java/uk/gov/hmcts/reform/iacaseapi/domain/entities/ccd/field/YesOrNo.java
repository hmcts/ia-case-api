package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum YesOrNo {

    NO("No"),
    YES("Yes");

    @JsonValue
    private final String id;

    YesOrNo(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
