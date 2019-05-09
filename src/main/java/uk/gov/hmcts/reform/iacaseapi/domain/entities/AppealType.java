package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum AppealType {

    RP("revocationOfProtection"),
    PA("protection");

    @JsonValue
    private String value;

    AppealType(String value) {
        this.value = value;
    }

    public static Optional<AppealType> from(
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
