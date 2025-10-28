package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum MakeAnApplicationTypes {

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
    CHANGE_DECISION_TYPE("Change decision type"),
    TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS("Transfer out of accelerated detained appeals process"),
    SET_ASIDE_A_DECISION("Set aside a decision"),
    APPLICATION_UNDER_RULE_31_OR_RULE_32("Application under rule 31 or rule 32"),
    OTHER("Other");

    @JsonValue
    private final String value;

    MakeAnApplicationTypes(String value) {
        this.value = value;
    }

    public static Optional<MakeAnApplicationTypes> getTypeFrom(String s) {
        return Arrays.stream(MakeAnApplicationTypes.values())
            .filter(v -> v.value.equals(s))
            .findFirst();
    }

    @Override
    public String toString() {
        return value;
    }
}
