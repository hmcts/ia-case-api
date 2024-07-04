package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LANGUAGE_READONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IN_CAMERA_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SINGLE_SEX_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateHearingRequirementsHandlerTest {

    private static final String LIST_YES = "Yes";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    @Mock
    private FeatureToggler featureToggler;
    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "30/01/2019";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "31/01/2019";
    private String applicationStatus = "In progress";

    private InterpreterLanguageRefData interpreterLanguage = new InterpreterLanguageRefData(
        new DynamicList(new Value("abc", "abc"), Collections.emptyList()),
        Collections.emptyList(),
        "");

    private List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
        Collections.emptyList(),
        applicationSupplier,
        ApplicationType.UPDATE_HEARING_REQUIREMENTS.toString(),
        applicationReason,
        applicationDate,
        applicationDecision,
        applicationDecisionReason,
        applicationDateOfDecision,
        applicationStatus
    )));

    private UpdateHearingRequirementsHandler updateHearingRequirementsHandler;

    @BeforeEach
    public void setUp() {
        updateHearingRequirementsHandler = new UpdateHearingRequirementsHandler(
            previousRequirementsAndRequestsAppender,
            featureToggler
        );

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
    }

    @Test
    void should_set_witness_count_to_zero_and_overview_page_flags() {

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(WITNESS_DETAILS);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.WITNESS_COUNT), eq(0));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE), eq(YES));
        verify(asylumCase)
            .write(eq(AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.UNKNOWN));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.UPDATE_HEARING_REQUIREMENTS_EXISTS), eq(YES));

        verify(asylumCase).clear(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS);
        // review fields should be cleared
        verify(asylumCase).clear(REVIEWED_HEARING_REQUIREMENTS);
        verify(asylumCase).clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(ADDITIONAL_TRIBUNAL_RESPONSE);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_witness_count_and_overview_page_flags() {

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(Arrays
            .asList(new IdValue("1", new WitnessDetails("cap")), new IdValue("2", new WitnessDetails("Pan")))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(WITNESS_DETAILS);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.WITNESS_COUNT), eq(2));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE), eq(YES));
        verify(asylumCase)
            .write(eq(AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.UNKNOWN));
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.UPDATE_HEARING_REQUIREMENTS_EXISTS), eq(YES));

        verify(asylumCase).clear(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS);
        // review fields should be cleared
        verify(asylumCase).clear(REVIEWED_HEARING_REQUIREMENTS);
        verify(asylumCase).clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase).clear(ADDITIONAL_TRIBUNAL_RESPONSE);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_append_and_trim_hearing_requirements_and_requests_when_ftpa_reheard() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(previousRequirementsAndRequestsAppender, times(1)).appendAndTrim(asylumCase);
    }

    @Test
    void should_not_append_and_trim_hearing_requirements_and_requests_when_ftpa_reheard_and_feature_flag_disabled() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(previousRequirementsAndRequestsAppender, times(0)).appendAndTrim(asylumCase);
    }

    @Test
    void should_not_trim_hearing_requirements_and_requests_when_feature_flag_disabled() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(previousRequirementsAndRequestsAppender, times(0)).appendAndTrim(asylumCase);
    }

    @Test
    void should_not_trim_hearing_requirements_and_requests_when_not_a_reheard_case() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(previousRequirementsAndRequestsAppender, times(0)).appendAndTrim(asylumCase);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_HEARING_REQUIREMENTS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> updateHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> updateHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_populate_appellant_interpreter_language_fields() {
        try (MockedStatic<HandlerUtils> mockedStatic = mockStatic(HandlerUtils.class)) {
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            mockedStatic.verify(() -> HandlerUtils.populateAppellantInterpreterLanguageFieldsIfRequired(asylumCase));
        }
    }

    @Test
    void should_sanitize_appellant_languages_when_manual_language_entered_and_manual_entry_deselected() {

        InterpreterLanguageRefData spokenInterpreterLanguage = new InterpreterLanguageRefData(
            new DynamicList(new Value("abc", "abc"), Collections.emptyList()),
            Collections.emptyList(),
            "manual");
        InterpreterLanguageRefData signInterpreterLanguage = new InterpreterLanguageRefData(
            new DynamicList(new Value("def", "def"), Collections.emptyList()),
            Collections.emptyList(),
            "manual");
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(),
                SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenInterpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.of(signInterpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenInterpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signInterpreterLanguage));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ArgumentCaptor<InterpreterLanguageRefData> languageCaptor = ArgumentCaptor
            .forClass(InterpreterLanguageRefData.class);
        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE), languageCaptor.capture());
        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE), languageCaptor.capture());
        assertNull(languageCaptor.getAllValues().get(0).getLanguageManualEntryDescription());
        assertNull(languageCaptor.getAllValues().get(1).getLanguageManualEntryDescription());
    }

    @Test
    void should_sanitize_appellant_languages_when_ref_data_language_selected_and_manual_entry_selected() {

        InterpreterLanguageRefData spokenInterpreterLanguage = new InterpreterLanguageRefData(
            new DynamicList(new Value("abc", "abc"), Collections.emptyList()),
            List.of(LIST_YES),
            "manual");
        InterpreterLanguageRefData signInterpreterLanguage = new InterpreterLanguageRefData(
            new DynamicList(new Value("def", "def"), Collections.emptyList()),
            List.of(LIST_YES),
            "manual");
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(),
                SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenInterpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.of(signInterpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenInterpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signInterpreterLanguage));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        ArgumentCaptor<InterpreterLanguageRefData> languageCaptor = ArgumentCaptor
            .forClass(InterpreterLanguageRefData.class);
        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE), languageCaptor.capture());
        verify(asylumCase).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE), languageCaptor.capture());
        assertNull(languageCaptor.getAllValues().get(0).getLanguageRefData());
        assertNull(languageCaptor.getAllValues().get(1).getLanguageRefData());
    }

    @Test
    void should_set_appellant_interpreter_spoken_language_when_only_spoken_language_category_selected() {

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);

    }

    @Test
    void should_set_appellant_interpreter_spoken_and_sign_language_when_spoken_and_sign_language_categories_selected() {

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(), SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase, times(0)).clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);

    }

    @Test
    void should_clear_all_appellant_interpreter_fields_if_interprerer_services_not_required() {

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(NO));
        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);
        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase, times(1)).clear(INTERPRETER_LANGUAGE);
        verify(asylumCase, times(1)).clear(INTERPRETER_LANGUAGE_READONLY);
    }

}
