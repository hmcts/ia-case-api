package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum YesOrNo {

    NO("No"),
    YES("Yes");

    @JsonValue
    private final String id;

    YesOrNo(String id) {
        this.id = id;
    }

    @JsonCreator
    public static YesOrNo fromString(String value) {
        if (value == null) {
            return null;
        }
        switch (value.toLowerCase()) {
            case "yes": return YES;
            case "no": return NO;
            default: return null; // or throw IllegalArgumentException
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
