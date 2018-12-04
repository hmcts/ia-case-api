package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static java.util.Arrays.stream;

import java.util.Optional;

public enum AsylumAppealType {
    RP("revocationOfProtection"), PA("protection");

    private String value;

    AsylumAppealType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<AsylumAppealType> from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    @Override
    public String toString() {
        return value;
    }
}
