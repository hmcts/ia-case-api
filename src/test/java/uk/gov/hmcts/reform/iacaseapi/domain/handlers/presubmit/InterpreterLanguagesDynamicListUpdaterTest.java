package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LIST_ELEMENT_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LIST_ELEMENT_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class InterpreterLanguagesDynamicListUpdaterTest {

    private static final String NO_WITNESSES_SELECTED_ERROR = "Select at least one witness";
    private static final String DRAFT_HEARING_REQUIREMENTS_PAGE_ID = "draftHearingRequirements";
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
    private CategoryValues categoryValues;
    @Mock
    private Value value;
    @Mock
    private WitnessDetails witnessDetails1;
    @Mock
    private WitnessDetails witnessDetails2;
    @Mock
    private DynamicMultiSelectList witnessListElement1;
    @Mock
    private DynamicMultiSelectList witnessListElement2;

    private RefDataUserService refDataUserService;
    private InterpreterLanguagesDynamicListUpdater interpreterLanguagesDynamicListUpdater;

    @BeforeEach
    void setup() {
        refDataUserService = mock(RefDataUserService.class);
        interpreterLanguagesDynamicListUpdater =
            new InterpreterLanguagesDynamicListUpdater(refDataUserService);

        when(callback.getEvent()).thenReturn(DRAFT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_populate_dynamic_list() {
        List<CategoryValues> languages = List.of(categoryValues);
        List<Value> values = List.of(value);

        when(callback.getEvent()).thenReturn(DRAFT_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(DRAFT_HEARING_REQUIREMENTS_PAGE_ID);
        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
                .thenReturn(commonDataResponse);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, INTERPRETER_LANGUAGES))
            .thenReturn(languages);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, SIGN_LANGUAGES))
                .thenReturn(languages);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(languages)).thenReturn(values);

        DynamicList dynamicListOfLanguages = new DynamicList(new Value("", ""), values);

        InterpreterLanguageRefData interpreterLanguageRefData = new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            Collections.emptyList(),
            "");

        interpreterLanguagesDynamicListUpdater.handle(MID_EVENT, callback);

        ArgumentCaptor<InterpreterLanguageRefData>  argumentCaptor = ArgumentCaptor.forClass(InterpreterLanguageRefData.class);

        verify(asylumCase, times(1)).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(interpreterLanguageRefData);

        verify(asylumCase, times(1)).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(interpreterLanguageRefData);
    }

    @Test
    void should_not_populate_dynamic_list_if_interpreter_language_ref_data_exists() {
        when(callback.getPageId()).thenReturn(DRAFT_HEARING_REQUIREMENTS_PAGE_ID);
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(asylumCaseBefore.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.empty());

        verify(refDataUserService, never()).retrieveCategoryValues(anyString(), anyString());
    }

    @Test
    void should_populate_dynamic_lists_for_witnesses() {
        when(callback.getPageId()).thenReturn(WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS, List.class)).thenReturn(Optional.of(List.of(witnessDetails1,witnessDetails2)));
        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        List<CategoryValues> languages = List.of(categoryValues);
        List<Value> values = List.of(value);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, INTERPRETER_LANGUAGES))
            .thenReturn(languages);
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, SIGN_LANGUAGES))
            .thenReturn(languages);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(languages)).thenReturn(values);
        when(asylumCase.read(WITNESS_LIST_ELEMENT_1, DynamicMultiSelectList.class)).thenReturn(Optional.of(witnessListElement1));
        when(asylumCase.read(WITNESS_LIST_ELEMENT_2, DynamicMultiSelectList.class)).thenReturn(Optional.of(witnessListElement2));
        List<Value> user = List.of(new Value("name lastname", "name lastName"));
        when(witnessListElement1.getValue()).thenReturn(user);
        when(witnessListElement2.getValue()).thenReturn(user);

        // selecting spoken interpreter for witness1 and sign interpreter for witness2
        when(asylumCase.read(WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("spokenLanguageInterpreter")));
        when(asylumCase.read(WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("signLanguageInterpreter")));

        interpreterLanguagesDynamicListUpdater.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE), any(InterpreterLanguageRefData.class));
        verify(asylumCase).write(eq(WITNESS_2_INTERPRETER_SIGN_LANGUAGE), any(InterpreterLanguageRefData.class));
    }

    @Test
    void should_add_ui_error_if_no_witness_is_selected() {
        when(callback.getPageId()).thenReturn(WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));

        PreSubmitCallbackResponse<AsylumCase> response = interpreterLanguagesDynamicListUpdater
            .handle(MID_EVENT, callback);

        assertTrue(response.getErrors().contains(NO_WITNESSES_SELECTED_ERROR));
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

                if (event.equals(DRAFT_HEARING_REQUIREMENTS)
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

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
