package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum ContactPreference {

    WANTS_EMAIL("wantsEmail", "Email"),
    WANTS_SMS("wantsSms", "Text message");

    @JsonValue
    private String value;

    private String description;

    ContactPreference(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Optional<ContactPreference> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
