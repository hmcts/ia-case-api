package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.RefDataUserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ApplicantInterpreterLanguagesDynamicListUpdaterTest {
    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    private BailCase bailCase;
    @Mock
    private BailCase asylumCaseBefore;
    @Mock
    private CommonDataResponse commonDataResponse;
    @Mock
    private InterpreterLanguageRefData spokenLanguages;
    @Mock
    private InterpreterLanguageRefData signLanguages;
    @Mock
    private InterpreterLanguageRefData spokenLanguagesSelected;
    @Mock
    private InterpreterLanguageRefData signLanguagesSelected;

    private MockedStatic<InterpreterLanguagesUtils> interpreterLanguagesUtils;

    private RefDataUserService refDataUserService;
    private InterpreterLanguagesDynamicListUpdater interpreterLanguagesDynamicListUpdater;

    @BeforeEach
    void setup() {
        refDataUserService = mock(RefDataUserService.class);
        interpreterLanguagesDynamicListUpdater =
            new InterpreterLanguagesDynamicListUpdater(refDataUserService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        interpreterLanguagesUtils = mockStatic(InterpreterLanguagesUtils.class);
    }

    @AfterEach
    void tearDown() {
        interpreterLanguagesUtils.close();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPLICATION",
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
        "MAKE_NEW_APPLICATION"
    })
    void should_populate_dynamic_lists_for_appellant_for_valid_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));

        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);

        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, INTERPRETER_LANGUAGES))
            .thenReturn(spokenLanguages);
        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, SIGN_LANGUAGES))
            .thenReturn(signLanguages);

        interpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(bailCase).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS1_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS1_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS2_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS2_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS3_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS3_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS4_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS4_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void should_populate_dynamic_lists_for_appellant_for_edit_application() {
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(asylumCaseBefore.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenLanguagesSelected));
        when(asylumCaseBefore.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.of(signLanguagesSelected));
        when(spokenLanguagesSelected.getLanguageManualEntry()).thenReturn("Yes");
        when(signLanguagesSelected.getLanguageManualEntry()).thenReturn("Yes");
        when(spokenLanguagesSelected.getLanguageManualEntryDescription()).thenReturn("desc");
        when(signLanguagesSelected.getLanguageManualEntryDescription()).thenReturn("desc");

        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);

        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, INTERPRETER_LANGUAGES))
            .thenReturn(spokenLanguages);
        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, SIGN_LANGUAGES))
            .thenReturn(signLanguages);

        interpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(spokenLanguages).setLanguageManualEntry("Yes");
        verify(signLanguages).setLanguageManualEntry("Yes");
        verify(spokenLanguages).setLanguageManualEntryDescription("desc");
        verify(signLanguages).setLanguageManualEntryDescription("desc");
        verify(bailCase).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS1_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS1_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS2_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS2_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS3_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS3_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase).write(FCS4_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase).write(FCS4_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void should_not_populate_dynamic_lists_if_data_already_present() {
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);
        final DynamicList spokenLanguage = new DynamicList(new Value("1", "English"), List.of(new Value("1", "English")));
        final InterpreterLanguageRefData spokenLanguageRefData = new InterpreterLanguageRefData(spokenLanguage,
                                                                                                "",
                                                                                                "");
        final DynamicList signLanguage = new DynamicList(new Value("1", "Makaton"), List.of(new Value("1", "Makaton")));
        final InterpreterLanguageRefData signLanguageRefData = new InterpreterLanguageRefData(signLanguage,
                                                                                                "",
                                                                                                "");

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenLanguageRefData));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signLanguageRefData));
        when(bailCase.read(FCS1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenLanguageRefData));
        when(bailCase.read(FCS1_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signLanguageRefData));
        when(bailCase.read(FCS2_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenLanguageRefData));
        when(bailCase.read(FCS2_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signLanguageRefData));
        when(bailCase.read(FCS3_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenLanguageRefData));
        when(bailCase.read(FCS3_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signLanguageRefData));
        when(bailCase.read(FCS4_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenLanguageRefData));
        when(bailCase.read(FCS4_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signLanguageRefData));

        interpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(bailCase, never()).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase, never()).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase, never()).write(FCS1_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase, never()).write(FCS1_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase, never()).write(FCS2_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase, never()).write(FCS2_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase, never()).write(FCS3_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase, never()).write(FCS3_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(bailCase, never()).write(FCS4_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(bailCase, never()).write(FCS4_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> interpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = interpreterLanguagesDynamicListUpdater.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START
                    && Set.of(START_APPLICATION, EDIT_BAIL_APPLICATION, EDIT_BAIL_APPLICATION_AFTER_SUBMIT, MAKE_NEW_APPLICATION).contains(callback.getEvent())) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> interpreterLanguagesDynamicListUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> interpreterLanguagesDynamicListUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> interpreterLanguagesDynamicListUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> interpreterLanguagesDynamicListUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
