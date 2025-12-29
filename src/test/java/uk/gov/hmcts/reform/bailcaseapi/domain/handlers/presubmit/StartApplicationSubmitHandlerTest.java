package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggleService;
import uk.gov.hmcts.reform.iacaseapi.domain.utils.InterpreterLanguagesUtils;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class StartApplicationSubmitHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock
    private BailCase bailCase;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private InterpreterLanguagesUtils interpreterLanguagesUtils;

    private StartApplicationSubmitHandler startApplicationSubmitHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        startApplicationSubmitHandler = new StartApplicationSubmitHandler(featureToggleService);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = startApplicationSubmitHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT && callback.getEvent() == Event.START_APPLICATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_handle_the_event() {
        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        startApplicationSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_set_bails_location_ref_data_field(boolean featureFlag) {
        when(featureToggleService.locationRefDataEnabled()).thenReturn(featureFlag);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        startApplicationSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);
        verify(bailCase, times(1)).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, featureFlag ? YesOrNo.YES : YesOrNo.NO);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> startApplicationSubmitHandler
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> startApplicationSubmitHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> startApplicationSubmitHandler
            .handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> startApplicationSubmitHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(null);
        assertThatThrownBy(() -> startApplicationSubmitHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(null);
        assertThatThrownBy(() -> startApplicationSubmitHandler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
