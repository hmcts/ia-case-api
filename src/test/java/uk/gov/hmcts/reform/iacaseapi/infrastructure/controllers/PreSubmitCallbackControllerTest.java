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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;

@RunWith(MockitoJUnitRunner.class)
public class PreSubmitCallbackControllerTest {

    @Mock private PreSubmitCallbackDispatcher<CaseDataMap> callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<CaseDataMap> callbackResponse;
    @Mock private Callback<CaseDataMap> callback;
    @Mock private CaseDetails<CaseDataMap> caseDetails;

    private PreSubmitCallbackController preSubmitCallbackController;

    @Before
    public void setUp() {
        preSubmitCallbackController =
            new PreSubmitCallbackController(
                callbackDispatcher
            );
    }

    @Test
    public void should_dispatch_about_to_start_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<CaseDataMap>> actualResponse =
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

        ResponseEntity<PreSubmitCallbackResponse<CaseDataMap>> actualResponse =
            preSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    public void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PreSubmitCallbackController(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
