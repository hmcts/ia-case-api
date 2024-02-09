package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum  MakeAnApplicationTypes {

    ADJOURN("Adjourn"),
    EXPEDITE("Expedite"),
    JUDGE_REVIEW("Judge's review of application decision"),
    LINK_OR_UNLINK("Link/unlink appeals"),
    TIME_EXTENSION("Time extension"),
    TRANSFER("Transfer"),
    WITHDRAW("Withdraw"),
    UPDATE_HEARING_REQUIREMENTS("Update hearing requirements"),
    UPDATE_APPEAL_DETAILS("Update appeal details"),
    REINSTATE("Reinstate an ended appeal"),
    APPLICATION_UNDER_RULE_31_OR_RULE_32("Application under rule 31 or rule 32"),
    OTHER("Other");

    @JsonValue
    private final String value;

    MakeAnApplicationTypes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
