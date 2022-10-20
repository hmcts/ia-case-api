package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StrategicCaseFlagType {

    COMPLEX_CASE("complexCase", "Complex Case", "CF0002"),
    DETAINED_INDIVIDUAL("detainedIndividual", "Detained individual", "PF0019"),
    FOREIGN_NATIONAL_OFFENDER("foreignNationalOffender", "Foreign national offender", "PF0012"),
    RRO_ANONYMISATION("rroAnonymisation",
        "RRO (Restricted Reporting Order / Anonymisation)",
        "CF0012"),
    UNACCEPTABLE_CUSTOMER_BEHAVIOUR("unacceptableCustomerBehaviour",
        "Unacceptable/disruptive customer behaviour",
        "PF0007"),
    UNACCOMPANIED_MINOR("unaccompaniedMinor", "Unaccompanied minor", "PF0013"),


    @JsonEnumDefaultValue
    UNKNOWN("unknown", "Unknown", "Unknown");

    @JsonValue
    private final String id;
    private final String readableText;
    private final String flagCode;

    StrategicCaseFlagType(String id, String readableText, String flagCode) {
        this.id = id;
        this.readableText = readableText;
        this.flagCode = flagCode;
    }

    public String getReadableText() {
        return readableText;
    }

    public String getFlagCode() {
        return flagCode;
    }

    @Override
    public String toString() {
        return id;
    }
}
