package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUPER_APPEAL_TYPE;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum SuperAppealType {

    RP("revocationOfProtection", "Revocation of a protection status"),
    PA("protection", "Refusal of protection claim"),
    EA("refusalOfEu", "Refusal of application under the EEA regulations"),
    HU("refusalOfHumanRights", "Refusal of a human rights claim"),
    DC("deprivation", "Deprivation of citizenship"),
    AG("ageAssessment", "Age assessment appeal");

    @JsonValue
    private String value;

    private String description;

    SuperAppealType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Optional<SuperAppealType> from(
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

    public static Optional<SuperAppealType> mapFromAsylumCaseAppealType(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        Optional<SuperAppealType> optionalSuperAppealType = asylumCase.read(SUPER_APPEAL_TYPE, SuperAppealType.class);

        if (optionalSuperAppealType.isEmpty() && optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return SuperAppealType.from(appealType.getValue());
        }

        return optionalSuperAppealType;
    }
}

