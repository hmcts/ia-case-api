package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HearingConductionOptions {
    DECISION_WITHOUT_HEARING("decisionWithoutHearing"),
    VIDEO_WITH_NO_PARTICIPANTS("videoWithNoParticipants"),
    VIDEO_WITH_AT_LEAST_ONE("videoWithAtLeastOne"),
    AUDIO_WITH_NO_PARTICIPANTS("audioWithNoParticipants"),
    AUDIO_WITH_AT_LEAST_ONE("audioWithAtLeastOne"),
    ALL_PARTICIPANTS("allParticipants");

    @JsonValue
    private final String id;

    HearingConductionOptions(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
