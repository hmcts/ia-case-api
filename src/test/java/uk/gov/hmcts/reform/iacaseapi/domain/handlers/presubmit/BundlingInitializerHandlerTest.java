package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_BUNDLES;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@Disabled
class BundlingInitializerHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<?>> caseBundlesCaptor;

    private BundlingInitializerHandler bundlingInitializerHandler;

    @BeforeEach
    public void setUp() {
        bundlingInitializerHandler = new BundlingInitializerHandler();
    }

    @Test
    void should_have_last_dispatch_priority() {
        assertEquals(DispatchPriority.LAST, bundlingInitializerHandler.getDispatchPriority());
    }

    @Test
    void should_clear_case_bundles_for_async_stitching_complete() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.FINAL_BUNDLING);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            bundlingInitializerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(CASE_BUNDLES, List.of());
    }

    @Test
    void should_clear_case_bundles_with_empty_list() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.FINAL_BUNDLING);

        bundlingInitializerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(eq(CASE_BUNDLES), caseBundlesCaptor.capture());

        List<?> caseBundles = caseBundlesCaptor.getValue();
        assertNotNull(caseBundles);
        assertTrue(caseBundles.isEmpty());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> bundlingInitializerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> bundlingInitializerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                for (State state : State.values()) {
                    when(caseDetails.getState()).thenReturn(state);

                    boolean canHandle = bundlingInitializerHandler.canHandle(callbackStage, callback);

                    if (event == Event.ASYNC_STITCHING_COMPLETE
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                        && state != State.FTPA_DECIDED) {
                        assertTrue(canHandle, "Should handle for event=" + event + ", stage=" + callbackStage + ", state=" + state);
                    } else {
                        assertFalse(canHandle, "Should not handle for event=" + event + ", stage=" + callbackStage + ", state=" + state);
                    }
                }
            }

            reset(callback, caseDetails);
        }
    }

    @Test
    void should_not_handle_when_state_is_ftpa_decided() {
        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.FTPA_DECIDED);

        boolean canHandle = bundlingInitializerHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_handle_when_state_is_not_ftpa_decided() {
        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.FINAL_BUNDLING);

        boolean canHandle = bundlingInitializerHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertTrue(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> bundlingInitializerHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bundlingInitializerHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
