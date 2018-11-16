package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Deserializer;

@RunWith(MockitoJUnitRunner.class)
public class PreSubmitCallbackControllerTest {

    @Mock private Deserializer<Callback<AsylumCase>> callbackDeserializer;
    @Mock private PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private PreSubmitCallbackController preSubmitCallbackController;

    @Before
    public void setUp() {
        preSubmitCallbackController =
            new PreSubmitCallbackController(
                callbackDeserializer,
                callbackDispatcher
            );
    }

    @Test
    public void should_deserialize_about_to_start_callback_then_dispatch_then_return_response() {

        String asylumCaseCallbackSource = "{\"case\":\"data\"}";

        doReturn(callback)
            .when(callbackDeserializer)
            .deserialize(asylumCaseCallbackSource);

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToStart(asylumCaseCallbackSource);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );
    }

    @Test
    public void should_deserialize_about_to_submit_callback_then_dispatch_then_return_response() {

        String asylumCaseCallbackSource = "{\"case\":\"data\"}";

        doReturn(callback)
            .when(callbackDeserializer)
            .deserialize(asylumCaseCallbackSource);

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdAboutToSubmit(asylumCaseCallbackSource);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    public void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PreSubmitCallbackController(null, callbackDispatcher))
            .hasMessage("callbackDeserializer must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PreSubmitCallbackController(callbackDeserializer, null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
