package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppellantInterpreterLanguagesDynamicListUpdaterTest {

    private static final String NO_WITNESSES_SELECTED_ERROR = "Select at least one witness";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;
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
    private AppellantInterpreterLanguagesDynamicListUpdater appellantInterpreterLanguagesDynamicListUpdater;

    @BeforeEach
    void setup() {
        refDataUserService = mock(RefDataUserService.class);
        appellantInterpreterLanguagesDynamicListUpdater =
            new AppellantInterpreterLanguagesDynamicListUpdater(refDataUserService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        interpreterLanguagesUtils = mockStatic(InterpreterLanguagesUtils.class);
    }

    @Test
    void should_populate_dynamic_lists_for_appellant_for_draft_hearing_requirements() {
        when(callback.getEvent()).thenReturn(DRAFT_HEARING_REQUIREMENTS);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails)); // asylumCase doesn't differ when drafting hearing reqs

        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);

        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, INTERPRETER_LANGUAGES))
            .thenReturn(spokenLanguages);
        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, SIGN_LANGUAGES))
            .thenReturn(signLanguages);

        appellantInterpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(asylumCase).write(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(asylumCase).write(APPELLANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void should_populate_dynamic_lists_for_appellant_for_update_hearing_requirements() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore)); // asylumCase doesn't differ when drafting hearing reqs
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(asylumCaseBefore.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenLanguagesSelected));
        when(asylumCaseBefore.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.of(signLanguagesSelected));
        when(spokenLanguagesSelected.getLanguageManualEntry()).thenReturn(List.of("Yes"));
        when(signLanguagesSelected.getLanguageManualEntry()).thenReturn(List.of("Yes"));
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

        appellantInterpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(spokenLanguages).setLanguageManualEntry(List.of("Yes"));
        verify(signLanguages).setLanguageManualEntry(List.of("Yes"));
        verify(spokenLanguages).setLanguageManualEntryDescription("desc");
        verify(signLanguages).setLanguageManualEntryDescription("desc");
        verify(asylumCase).write(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(asylumCase).write(APPELLANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> appellantInterpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appellantInterpreterLanguagesDynamicListUpdater.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START
                    && Set.of(DRAFT_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS).contains(callback.getEvent())) {

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

        assertThatThrownBy(() -> appellantInterpreterLanguagesDynamicListUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> appellantInterpreterLanguagesDynamicListUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantInterpreterLanguagesDynamicListUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appellantInterpreterLanguagesDynamicListUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
