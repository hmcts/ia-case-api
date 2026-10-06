package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum YesOrNo {

    NO("No"),
    YES("Yes");

    @JsonValue
    private final String id;

    @JsonCreator
    YesOrNo(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean isYes() {
        return this == YES;
    }

    public boolean isNo() {
        return this == NO;
    }
}
