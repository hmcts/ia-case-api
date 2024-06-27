package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum AppealType {

    RP("revocationOfProtection", "Revocation of a protection status"),
    PA("protection", "Refusal of protection claim"),
    EA("refusalOfEu", "Refusal of application under the EEA regulations"),
    HU("refusalOfHumanRights", "Refusal of a human rights claim"),
    DC("deprivation", "Deprivation of citizenship"),
    EU("euSettlementScheme", "EU Settlement Scheme"),
    AG("ageAssessment", "Age assessment appeal");


    @JsonValue
    private final String value;

    private final String description;

    AppealType(String value, String description) {
        this.value = value;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
