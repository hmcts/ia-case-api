package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CaseFlagType {

    ANONYMITY("anonymity"),
    COMPLEX_CASE("complexCase"),
    DETAINED_IMMIGRATION_APPEAL("detainedImmigrationAppeal"),
    FOREIGN_NATIONAL_OFFENDER("foreignNationalOffender"),
    POTENTIALLY_VIOLENT_PERSON("potentiallyViolentPerson"),
    UNACCEPTABLE_CUSTOMER_BEHAVIOUR("unacceptableCustomerBehaviour"),
    UNACCOMPANIED_MINOR("unaccompaniedMinor"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    CaseFlagType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
