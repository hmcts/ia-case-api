package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeNewApplicationService;

@ExtendWith(MockitoExtension.class)
class MakeNewApplicationAboutToStartHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock
    private BailCase bailCase;
    @Mock
    private MakeNewApplicationService makeNewApplicationService;

    private MakeNewApplicationAboutToStartHandler makeNewApplicationAboutToStartHandler;

    @BeforeEach
    public void setUp() {
        makeNewApplicationAboutToStartHandler =
            new MakeNewApplicationAboutToStartHandler(makeNewApplicationService);
    }

    @Test
    void should_handle_clear_fields_about_to_start() {

        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        PreSubmitCallbackResponse<BailCase> response =
            makeNewApplicationAboutToStartHandler.handle(ABOUT_TO_START, callback, callbackResponse);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();

        verify(makeNewApplicationService, times(1)).clearFieldsAboutToStart(bailCase);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = makeNewApplicationAboutToStartHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START && callback.getEvent() == Event.MAKE_NEW_APPLICATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> makeNewApplicationAboutToStartHandler
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeNewApplicationAboutToStartHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeNewApplicationAboutToStartHandler
            .handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeNewApplicationAboutToStartHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> makeNewApplicationAboutToStartHandler.handle(ABOUT_TO_START, callback,
                                                                              callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        assertThatThrownBy(() -> makeNewApplicationAboutToStartHandler.handle(ABOUT_TO_START, callback,
                                                                              callbackResponse))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
