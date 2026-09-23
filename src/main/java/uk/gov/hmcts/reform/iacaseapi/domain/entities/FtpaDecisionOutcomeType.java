package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FtpaDecisionOutcomeType {
    NOT_ADMITTED("notAdmitted"),
    PARTIALLY_GRANTED("partiallyGranted"),
    GRANTED("granted"),
    REFUSED("refused");

    @JsonValue
    private final String id;

    @JsonCreator
    FtpaDecisionOutcomeType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
