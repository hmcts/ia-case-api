package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BailApplicationStatus {
    YES("Yes"),
    NO("No"),
    YES_WITHOUT_BAIL_APPLICATION_NUMBER("YesWithoutBailApplicationNumber"),
    NOT_SURE("NotSure");

    @JsonValue
    private final String id;

    BailApplicationStatus(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
