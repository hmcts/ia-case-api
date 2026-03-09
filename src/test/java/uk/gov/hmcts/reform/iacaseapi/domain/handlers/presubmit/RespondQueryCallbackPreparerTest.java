package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

class RespondQueryCallbackPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetailsHelper userDetailsHelper;

    private RespondQueryCallbackPreparer handler;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        handler = new RespondQueryCallbackPreparer();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_true_when_correct_stage_and_event() {

        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RESPOND_QUERY);

        boolean result = handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
        );

        assertTrue(result);
    }

    @Test
    void should_return_false_when_stage_is_wrong() {

        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RESPOND_QUERY);

        boolean result = handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        assertFalse(result);
    }

    @Test
    void should_return_false_when_event_is_wrong() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        boolean result = handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
        );

        assertFalse(result);
    }

    @Test
    void should_return_callback_response() {

        when(callback.getEvent()).thenReturn(Event.QUERY_MANAGEMENT_RESPOND_QUERY);

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertEquals(asylumCase, response.getData());
    }

    @Test
    void should_throw_exception_if_cannot_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback)
        );
    }
}