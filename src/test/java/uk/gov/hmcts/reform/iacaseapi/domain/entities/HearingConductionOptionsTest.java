
package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class HearingConductionOptionsTest {

    @Test
    public void has_correct_values() {
        assertEquals("decisionWithoutHearing", HearingConductionOptions.DECISION_WITHOUT_HEARING.toString());
        assertEquals("videoWithNoParticipants", HearingConductionOptions.VIDEO_WITH_NO_PARTICIPANTS.toString());
        assertEquals("videoWithAtLeastOne", HearingConductionOptions.VIDEO_WITH_AT_LEAST_ONE.toString());
        assertEquals("audioWithNoParticipants", HearingConductionOptions.AUDIO_WITH_NO_PARTICIPANTS.toString());
        assertEquals("audioWithAtLeastOne", HearingConductionOptions.AUDIO_WITH_AT_LEAST_ONE.toString());
        assertEquals("allParticipants", HearingConductionOptions.ALL_PARTICIPANTS.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(6, HearingConductionOptions.values().length);
    }
}
