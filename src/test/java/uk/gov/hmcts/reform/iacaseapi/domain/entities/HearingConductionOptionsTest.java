package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HearingConductionOptionsTest {

    @Test
    void has_correct_values() {
        assertEquals("decisionWithouHearing", HearingConductionOptions.DECISION_WITHOUT_HEARING.toString());
        assertEquals("videoWithNoParticipants", HearingConductionOptions.VIDEO_WITH_NO_PARTICIPANTS.toString());
        assertEquals("videoWithAtLeastOne", HearingConductionOptions.VIDEO_WITH_AT_LEAST_ONE.toString());
        assertEquals("audioWithNoParticipants", HearingConductionOptions.AUDIO_WITH_NO_PARTICIPANTS.toString());
        assertEquals("audioWithAtLeastOne", HearingConductionOptions.AUDIO_WITH_AT_LEAST_ONE.toString());
        assertEquals("allParticipants", HearingConductionOptions.ALL_PARTICIPANTS.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, HearingConductionOptions.values().length);
    }
}
