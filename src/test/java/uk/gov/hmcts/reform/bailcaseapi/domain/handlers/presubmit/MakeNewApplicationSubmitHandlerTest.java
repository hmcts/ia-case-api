package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.MakeNewApplicationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class MakeNewApplicationSubmitHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock
    private BailCase bailCase;
    @Mock
    private BailCase bailCaseBefore;
    @Mock
    private MakeNewApplicationService makeNewApplicationService;


    private MakeNewApplicationSubmitHandler makeNewApplicationSubmitHandler;


    @BeforeEach
    public void setUp() {
        makeNewApplicationSubmitHandler =
            new MakeNewApplicationSubmitHandler(makeNewApplicationService);
    }

    @Test
    void should_handle_make_new_application() {

        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);

        PreSubmitCallbackResponse<BailCase> response =
            makeNewApplicationSubmitHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();

        verify(makeNewApplicationService, times(1)).clearUnrelatedFields(bailCase);
        verify(makeNewApplicationService, times(1)).appendPriorApplication(bailCase, bailCaseBefore);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = makeNewApplicationSubmitHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT && callback.getEvent() == Event.MAKE_NEW_APPLICATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_throw_when_case_details_before_is_not_present() {

        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
           makeNewApplicationSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Case details before missing")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> makeNewApplicationSubmitHandler
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeNewApplicationSubmitHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeNewApplicationSubmitHandler
            .handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeNewApplicationSubmitHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> makeNewApplicationSubmitHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        assertThatThrownBy(() -> makeNewApplicationSubmitHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
