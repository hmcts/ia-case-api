package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RespondentReviewAppealResponseAddedUpdaterMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private RespondentReviewAppealResponseAddedUpdaterMidEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondentReviewAppealResponseAddedUpdaterMidEventHandler();
    }

    @Test
    void it_can_handle_callback_for_mid_event_and_end_appeal_event_only() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");

        assertThatThrownBy(() -> handler.handle(null, callback))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_if_cannot_handle() {

        assertThatThrownBy(() ->
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }
}