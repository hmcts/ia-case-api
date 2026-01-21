package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.PreSubmitCallbackDispatcher;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.BailPreSubmitCallbackController;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BailPreSubmitCallbackControllerTest {
    @Mock
    private PreSubmitCallbackDispatcher<BailCase> callbackDispatcher;
    @Mock
    private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;

    private BailPreSubmitCallbackController bailPreSubmitCallbackController;

    @BeforeEach
    public void setUp() {

        bailPreSubmitCallbackController =
            new BailPreSubmitCallbackController(
                callbackDispatcher
            );
    }

    @Test
    void should_dispatch_about_to_start_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> actualResponse =
            bailPreSubmitCallbackController.ccdAboutToStart(callback);

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

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> actualResponse =
            bailPreSubmitCallbackController.ccdAboutToSubmit(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );
    }

    @Test
    void should_dispatch_mid_event_callback_then_return_response() {

        String pageIdParam = "somePageId";
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        doCallRealMethod().when(callback).setPageId(pageIdParam);
        doCallRealMethod().when(callback).getPageId();

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> actualResponse =
            bailPreSubmitCallbackController.ccdMidEvent(callback, pageIdParam);

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
        doCallRealMethod().when(callback).getPageId();

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> actualResponse =
            bailPreSubmitCallbackController.ccdMidEvent(callback, pageIdParam);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(
            PreSubmitCallbackStage.MID_EVENT,
            callback
        );
        assertEquals(pageIdParam, callback.getPageId());
    }

    @Test
    void should_fail_for_null_constructor_args() {
        assertThatThrownBy(() -> new BailPreSubmitCallbackController(null))
            .hasMessage("callbackDispatcher can not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
