package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum ContactPreferenceUnrep {

    WANTS_EMAIL("wantsEmail"),
    WANTS_SMS("wantsSms"),
    WANTS_POST("wantsPost");

    @JsonValue
    private String value;

    ContactPreferenceUnrep(String value) {
        this.value = value;
    }

    public static Optional<ContactPreferenceUnrep> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
