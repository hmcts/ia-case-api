package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum AppealTypeForFilter {

    PA("protection", "Protection"),
    HU("refusalOfHumanRights", "Human Rights"),
    RP("revocationOfProtection", "Revocation of Protection Status"),
    DC("deprivation", "Deprivation of citizenship"),
    EA("refusalOfEu", "European Economic Area"),
    EU("euSettlementScheme", "EU Settlement Scheme"),
    AG("ageAssessment", "Age Assessment");

    @JsonValue
    private String value;

    private String description;

    AppealTypeForFilter(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Optional<AppealTypeForFilter> from(
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
