package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class RepeatApplyNoticeOfChangeTest {

    private RepeatApplyNoticeOfChange repeatApplyNoticeOfChange;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;


    @BeforeEach
    public void setUp() throws Exception {
        repeatApplyNoticeOfChange = new RepeatApplyNoticeOfChange();
    }

    @Test
    public void calls_setOrganisationToRemove_and_returns_result() {
        AsylumCase sentCase = new AsylumCase();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(sentCase);
        when(callback.getEvent()).thenReturn(Event.REPEAT_APPLY_NOC);

        PreSubmitCallbackResponse<AsylumCase> response = repeatApplyNoticeOfChange.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        assertEquals(new AsylumCase(), response.getData());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = repeatApplyNoticeOfChange.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && event == Event.REPEAT_APPLY_NOC) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }
}
