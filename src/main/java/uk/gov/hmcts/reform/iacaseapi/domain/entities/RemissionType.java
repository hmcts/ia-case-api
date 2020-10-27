package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RemissionType {

    NO_REMISSION("noRemission"),
    HO_WAIVER_REMISSION("hoWaiverRemission"),
    HELP_WITH_FEES("helpWithFees");

    @JsonValue
    private final String id;

    RemissionType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
