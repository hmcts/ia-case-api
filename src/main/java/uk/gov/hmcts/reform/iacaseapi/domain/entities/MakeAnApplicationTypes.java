package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum  MakeAnApplicationTypes {

    ADJOURN("Adjourn"),
    EXPEDITE("Expedite"),
    JUDGE_REVIEW("Judge's review of application decision"),
    JUDGE_REVIEW_LO("Judge's review of Legal Officer decision"),
    LINK_OR_UNLINK("Link/unlink appeals"),
    TIME_EXTENSION("Time extension"),
    TRANSFER("Transfer"),
    WITHDRAW("Withdraw"),
    UPDATE_HEARING_REQUIREMENTS("Update hearing requirements"),
    UPDATE_APPEAL_DETAILS("Update appeal details"),
    REINSTATE("Reinstate an ended appeal"),
    TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS("Transfer out of accelerated detained appeals process"),
    SET_ASIDE_A_DECISION("Set aside a decision"),
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
