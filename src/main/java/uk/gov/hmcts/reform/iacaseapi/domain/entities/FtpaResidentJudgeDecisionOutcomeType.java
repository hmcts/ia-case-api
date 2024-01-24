package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FtpaResidentJudgeDecisionOutcomeType {

    GRANTED("granted"),
    PARTIALLY_GRANTED("partiallyGranted"),
    REFUSED("refused"),
    APPLICATION_NOT_ADMITTED("notAdmit"),
    WITHDRAWN("withdrawn"),
    REHEARD_RULE35("reheardRule35"),
    REMADE_RULE31("remadeRule31"),
    REMADE_RULE32("remadeRule32");

    @JsonValue
    private final String id;

    FtpaResidentJudgeDecisionOutcomeType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
