package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum MakeAnApplicationTypes {

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
