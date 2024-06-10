package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreSubmitCallbackControllerTest {

    @Mock
    private PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private PreSubmitCallbackController preSubmitCallbackController;

    @BeforeEach
    public void setUp() {

        preSubmitCallbackController =
            new PreSubmitCallbackController(
                callbackDispatcher
            );
    }

    @Test
    void should_dispatch_about_to_start_callback_then_return_response() {

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
    void should_dispatch_about_to_submit_callback_then_return_response() {

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

    @ParameterizedTest
    @ValueSource(strings = {"somePageId", ""})
    void should_dispatch_mid_event_callback_then_return_response(String pageIdParam) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        doCallRealMethod().when(callback).setPageId(pageIdParam);
        doCallRealMethod().when(callback).getPageId();

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdMidEvent(callback, pageIdParam);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.MID_EVENT,
            callback
        );
        assertEquals(pageIdParam, callback.getPageId());
    }

    @Test
    void should_dispatch_mid_event_callback_withou_breaking_if_pageId_null() {

        String pageIdParam = null;
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        doCallRealMethod().when(callback).setPageId(pageIdParam);
        doCallRealMethod().when(callback).getPageId();

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> actualResponse =
            preSubmitCallbackController.ccdMidEvent(callback, pageIdParam);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.MID_EVENT,
            callback
        );
        assertEquals(pageIdParam, callback.getPageId());
    }

    @Test
    void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PreSubmitCallbackController(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
