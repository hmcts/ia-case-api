package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NLR_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateNlrHearingRequirementsMidEventHandler.SIGN_ERROR;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateNlrHearingRequirementsMidEventHandler.SIGN_MANUAL_ERROR;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateNlrHearingRequirementsMidEventHandler.SPOKEN_ERROR;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateNlrHearingRequirementsMidEventHandler.SPOKEN_MANUAL_ERROR;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UpdateNlrHearingRequirementsMidEventTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private InterpreterLanguageRefData interpreterLanguageRefData;

    private UpdateNlrHearingRequirementsMidEventHandler updateNlrHearingRequirementsMidEventHandler;

    @BeforeEach
    public void setUp() {
        updateNlrHearingRequirementsMidEventHandler = new UpdateNlrHearingRequirementsMidEventHandler();

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("nlrHearingRequirementsPage");
    }

    @Test
    void it_can_handle_callback() {
        assertTrue(updateNlrHearingRequirementsMidEventHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"MID_EVENT"})
    void it_cannot_handle_callback_bad_stage(PreSubmitCallbackStage callbackStage) {
        assertFalse(updateNlrHearingRequirementsMidEventHandler.canHandle(callbackStage, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"UPDATE_HEARING_REQUIREMENTS"})
    void it_cannot_handle_callback_bad_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(updateNlrHearingRequirementsMidEventHandler.canHandle(MID_EVENT, callback));
    }

    @Test
    void it_cannot_handle_callback_bad_page() {
        when(callback.getPageId()).thenReturn("someOtherPage");
        assertFalse(updateNlrHearingRequirementsMidEventHandler.canHandle(MID_EVENT, callback));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateNlrHearingRequirementsMidEventHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> updateNlrHearingRequirementsMidEventHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateNlrHearingRequirementsMidEventHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateNlrHearingRequirementsMidEventHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateNlrHearingRequirementsMidEventHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateNlrHearingRequirementsMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void does_nothing_if_no_needs_interpreter() {
        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void does_nothing_if_needs_interpreter_is_yes_category_empty() {
        when(asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void errors_if_needs_interpreter_is_yes_category_contains_both_manual_but_empty_description() {
        when(asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(NLR_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(interpreterLanguageRefData.getLanguageManualEntry()).thenReturn(List.of("Yes"));
        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(2, response.getErrors().size());
        assertTrue(response.getErrors().contains(SPOKEN_MANUAL_ERROR));
        assertTrue(response.getErrors().contains(SIGN_MANUAL_ERROR));
    }

    @Test
    void does_nothing_if_needs_interpreter_is_yes_category_contains_both_manual_non_empty_description() {
        when(asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(NLR_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(interpreterLanguageRefData.getLanguageManualEntry()).thenReturn(List.of("Yes"));
        when(interpreterLanguageRefData.getLanguageManualEntryDescription()).thenReturn("anything");

        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }


    @Test
    void errors_if_needs_interpreter_is_yes_category_contains_both_refData_without_value() {
        when(asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(NLR_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(interpreterLanguageRefData.getLanguageRefData()).thenReturn(new DynamicList(null, List.of()));
        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(2, response.getErrors().size());
        assertTrue(response.getErrors().contains(SPOKEN_ERROR));
        assertTrue(response.getErrors().contains(SIGN_ERROR));
    }

    @Test
    void does_nothing_if_needs_interpreter_is_yes_category_contains_both_refData_with_value() {
        when(asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(NLR_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(NLR_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(asylumCase.read(NLR_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefData));
        when(interpreterLanguageRefData.getLanguageRefData()).thenReturn(new DynamicList(new Value("code", "label"), List.of()));

        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void errors_if_needs_interpreter_is_yes_category_contains_both_but_empty_fields() {
        when(asylumCase.read(IS_NLR_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(NLR_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue())));

        PreSubmitCallbackResponse<AsylumCase> response = updateNlrHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(2, response.getErrors().size());
        assertTrue(response.getErrors().contains(SPOKEN_ERROR));
        assertTrue(response.getErrors().contains(SIGN_ERROR));
    }
}