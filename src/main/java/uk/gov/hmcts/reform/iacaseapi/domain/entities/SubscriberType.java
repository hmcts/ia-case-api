package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum SubscriberType {

    APPELLANT("appellant"),
    SUPPORTER("supporter");

    @JsonValue
    private final String value;

    SubscriberType(String value) {
        this.value = value;
    }

    public static Optional<uk.gov.hmcts.reform.iacaseapi.domain.entities.SubscriberType> from(
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
        return getValue();
    }
}
