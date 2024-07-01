package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum OutOfCountryDecisionType {

    REFUSAL_OF_HUMAN_RIGHTS("refusalOfHumanRights", "A decision to refuse a human rights claim for entry clearance"),
    REFUSAL_OF_PROTECTION("refusalOfProtection", "A decision to refuse a human rights or protection claim, or deprive you of British citizenship, where you can only apply after your client has left the country"),
    REMOVAL_OF_CLIENT("removalOfClient", "A decision to remove your client under the Immigration (European Economic Area) Regulations 2016"),
    REFUSE_PERMIT("refusePermit", "A decision to refuse a permit to enter the UK or entry clearance under the immigration rules and/or the EU Settlement Scheme.");

    @JsonValue
    private String value;

    private String description;

    OutOfCountryDecisionType(String id,String description) {
        this.value = id;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<OutOfCountryDecisionType> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
