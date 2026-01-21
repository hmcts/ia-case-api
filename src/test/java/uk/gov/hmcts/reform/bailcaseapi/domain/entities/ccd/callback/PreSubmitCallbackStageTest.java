package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PreSubmitCallbackStageTest {

    @Test
    void has_correct_case_event_ids() {
        assertEquals("aboutToStart", PreSubmitCallbackStage.ABOUT_TO_START.toString());
        assertEquals("aboutToSubmit", PreSubmitCallbackStage.ABOUT_TO_SUBMIT.toString());
        assertEquals("midEvent", PreSubmitCallbackStage.MID_EVENT.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, PreSubmitCallbackStage.values().length);
    }
}
