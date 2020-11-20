package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PreSubmitCallbackControllerTest {

    @Mock private PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private FeatureToggler featureToggler;
    @Mock private AsylumCase asylumCase;

    private PreSubmitCallbackController preSubmitCallbackController;

    @BeforeEach
    public void setUp() {
        when(featureToggler.getValue("suspend-submit-case", false)).thenReturn(false);

        preSubmitCallbackController =
            new PreSubmitCallbackController(
                callbackDispatcher,
                featureToggler
            );
    }

    @Test
    public void should_dispatch_about_to_start_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToStart(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
    }

    @Test
    public void should_dispatch_about_to_submit_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    public void should_dispatch_mid_event_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdMidEvent(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.MID_EVENT,
            callback
        );
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "SUBMIT_APPEAL", "PAY_AND_SUBMIT_APPEAL" })
    public void should_not_dispatch_callback_when_suspend_flag_is_true_and_event_is_submit_appeal(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("suspend-submit-case", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().getErrors().contains("There is planned maintenance work in progress. Your appeal has been saved and you will be able to submit it later. Please try again in few hours."));
        verifyNoInteractions(callbackDispatcher);
    }

    @Test
    public void should_dispatch_callback_when_suspend_flag_is_true_and_event_is_not_submit_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().getErrors().isEmpty());
        verifyNoInteractions(featureToggler);
        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    public void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PreSubmitCallbackController(null, featureToggler))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
