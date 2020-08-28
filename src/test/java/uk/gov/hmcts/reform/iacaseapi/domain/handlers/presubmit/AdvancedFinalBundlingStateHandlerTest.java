package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STITCHING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.em.Bundle;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class AdvancedFinalBundlingStateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    AdvancedFinalBundlingStateHandler advancedFinalBundlingStateHandler;

    @BeforeEach
    void setUp() {
        advancedFinalBundlingStateHandler = new AdvancedFinalBundlingStateHandler();

        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.FINAL_BUNDLING);

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.empty(), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));
    }

    @Test
    void should_successfully_retain_the_current_state() {

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            advancedFinalBundlingStateHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_BUNDLES);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");

        assertEquals(State.FINAL_BUNDLING, returnedCallbackResponse.getState());
    }

    @Test
    void should_successfully_change_the_current_state_to_pre_hearing() {

        Bundle finishedBundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("DONE"), Optional.empty(), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.clear();
        caseBundles.add(new IdValue<>("1", finishedBundle));

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            advancedFinalBundlingStateHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_BUNDLES);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "DONE");

        assertEquals(State.PRE_HEARING, returnedCallbackResponse.getState());
    }

    @Test
    void should_throw_when_case_bundle_is_not_present() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_empty() {

        caseBundles.clear();

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("case bundles size is not 1 and is : 0")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.handle(ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = advancedFinalBundlingStateHandler.canHandle(callbackStage, callback);

                if (event == Event.ASYNC_STITCHING_COMPLETE
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedFinalBundlingStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
