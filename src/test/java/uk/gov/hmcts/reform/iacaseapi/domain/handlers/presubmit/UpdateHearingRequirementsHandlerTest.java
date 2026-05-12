package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;

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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UpdateHearingRequirementsHandlerTest {

    private static final String LIST_YES = "Yes";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase oldAsylumCase;
    @Mock
    private WitnessDetails witnessDetails1;
    @Mock
    private WitnessDetails witnessDetails2;
    @Mock
    private WitnessDetails witnessDetails3;
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
    private InterpreterLanguageRefData interpreterLanguage2 = new InterpreterLanguageRefData(
        new DynamicList(new Value("def", "def"), Collections.emptyList()),
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
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(oldAsylumCase);
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

        // review decision display fields should be cleared
        verify(asylumCase).clear(VULNERABILITIES_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(REMOTE_HEARING_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(MULTIMEDIA_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(IN_CAMERA_COURT_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(OTHER_DECISION_FOR_DISPLAY);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_witness_count_and_overview_page_flags() {

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(Arrays
            .asList(new IdValue("1", new WitnessDetails("cap", "cap")), new IdValue("2", new WitnessDetails("Pan", "Pan")))));

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

        // review decision display fields should be cleared
        verify(asylumCase).clear(VULNERABILITIES_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(REMOTE_HEARING_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(MULTIMEDIA_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(IN_CAMERA_COURT_DECISION_FOR_DISPLAY);
        verify(asylumCase).clear(OTHER_DECISION_FOR_DISPLAY);

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
    void should_set_appellant_interpreter_sign_language_when_only_sign_language_category_selected() {

        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase, times(0)).clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);

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
    }

    @Test
    void should_clear_all_witness_related_fields_if_no_witnesses_attending() {

        List<IdValue<WitnessDetails>> twoOldWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoOldWitnesses));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoOldWitnesses));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(NO));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        WITNESS_N_FIELD.forEach(field -> verify(asylumCase, times(1)).clear(field));
        WITNESS_N_INTERPRETER_CATEGORY_FIELD.forEach(field -> verify(asylumCase, times(1))
            .write(field, Collections.emptyList()));
        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(field -> verify(asylumCase, times(1)).clear(field));
        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(field -> verify(asylumCase, times(1)).clear(field));
        verify(asylumCase, times(1)).clear(WITNESS_DETAILS_READONLY);
        verify(asylumCase, times(1)).write(WITNESS_COUNT, 0);
        verify(asylumCase, times(1)).write(IS_ANY_WITNESS_INTERPRETER_REQUIRED, NO);
        verify(asylumCase, times(1)).clear(WITNESS_DETAILS);
        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);
        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase, times(1)).clear(APPELLANT_INTERPRETER_SIGN_LANGUAGE);
    }

    /*
    This condition is for safety only. When isWitnessAttending=Yes, the UI will force the user to add
    at least one witness to the collection. Fields are already cleared in previous steps when the user
    selects isWitnessAttending=No.
     */
    @Test
    void should_clear_all_witness_related_fields_if_no_witnesses_attending_but_collection_not_empty() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(oldAsylumCase);

        List<IdValue<WitnessDetails>> twoOldWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoOldWitnesses));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoOldWitnesses));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(YES));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        int i = 2;
        while (i < 10) {
            verify(asylumCase, times(1)).clear(WITNESS_N_FIELD.get(i));
            i++;
        }

        int j = 0;
        while (j < 10) {
            verify(asylumCase, times(1)).write(WITNESS_N_INTERPRETER_CATEGORY_FIELD.get(j), Collections.emptyList());
            verify(asylumCase, times(1)).clear(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(j));
            verify(asylumCase, times(1)).clear(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(j));
            j++;
        }

        verify(asylumCase, times(1)).write(WITNESS_COUNT, 2);
        verify(asylumCase).write(eq(WITNESS_1), any(WitnessDetails.class));
        verify(asylumCase).write(eq(WITNESS_2), any(WitnessDetails.class));

        verify(asylumCase).write(WITNESS_COUNT, 2);
        verify(asylumCase).clear(WITNESS_3);
        verify(asylumCase).clear(WITNESS_4);
        verify(asylumCase).clear(WITNESS_5);
        verify(asylumCase).clear(WITNESS_6);
        verify(asylumCase).clear(WITNESS_7);
        verify(asylumCase).clear(WITNESS_8);
        verify(asylumCase).clear(WITNESS_9);
        verify(asylumCase).clear(WITNESS_10);
    }

    /*
    If witness1 isn't deleted, witness2 is deleted, witness3 isn't deleted; but interpreters are not needed
    for witnesses, it should clear all witness related fields (e.g. languages etc.) but should still filter out
    the deleted witnesses and compress the list with reindexed idValues, so that witness1 remains witness1 and
    witness3 becomes witness2 (with idValue with id:2)
     */
    @Test
    void should_transpose_the_deleted_witnesses_and_clear_related_fields_if_interpreters_not_needed() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(oldAsylumCase);
        when(witnessDetails1.getWitnessName()).thenReturn("A");
        when(witnessDetails1.getWitnessFamilyName()).thenReturn("a");
        when(witnessDetails1.buildWitnessFullName()).thenReturn("A a");
        when(witnessDetails1.getWitnessPartyId()).thenReturn("partyId1");
        when(witnessDetails2.getWitnessName()).thenReturn("B");
        when(witnessDetails2.getWitnessFamilyName()).thenReturn("b");
        when(witnessDetails2.buildWitnessFullName()).thenReturn("B b");
        when(witnessDetails2.getIsWitnessDeleted()).thenReturn(YES);
        when(witnessDetails2.getWitnessPartyId()).thenReturn("partyId2");
        when(witnessDetails3.getWitnessName()).thenReturn("C");
        when(witnessDetails3.getWitnessFamilyName()).thenReturn("c");
        when(witnessDetails3.buildWitnessFullName()).thenReturn("C c");
        when(witnessDetails3.getWitnessPartyId()).thenReturn("partyId3");

        List<IdValue<WitnessDetails>> twoOldWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2));

        List<IdValue<WitnessDetails>> twoCurrentWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("3", witnessDetails3));

        // witnessDetails1 appears in new list: it wasn't deleted
        // witnessDetails2 doesn't appear in new list: it was deleted
        // witnessDetails3 only appears in new list: it was added anew

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoCurrentWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoOldWitnesses));

        when(asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(YES));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        WITNESS_N_INTERPRETER_CATEGORY_FIELD.forEach(field -> verify(asylumCase, times(1))
            .write(field, Collections.emptyList()));
        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(field -> verify(asylumCase, times(1)).clear(field));
        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(field -> verify(asylumCase, times(1)).clear(field));

        ArgumentCaptor<WitnessDetails> witnessDetailsCaptor = ArgumentCaptor.forClass(WitnessDetails.class);
        verify(asylumCase).write(eq(WITNESS_1), witnessDetailsCaptor.capture());
        verify(asylumCase).write(eq(WITNESS_2), witnessDetailsCaptor.capture());
        assertEquals("A", witnessDetailsCaptor.getAllValues().get(0).getWitnessName());
        assertEquals("C", witnessDetailsCaptor.getAllValues().get(1).getWitnessName());
        assertEquals("a", witnessDetailsCaptor.getAllValues().get(0).getWitnessFamilyName());
        assertEquals("c", witnessDetailsCaptor.getAllValues().get(1).getWitnessFamilyName());

        verify(asylumCase).write(eq(WITNESS_DETAILS), anyList());

        verify(asylumCase).write(WITNESS_COUNT, 2);
        verify(asylumCase).clear(WITNESS_3);
        verify(asylumCase).clear(WITNESS_4);
        verify(asylumCase).clear(WITNESS_5);
        verify(asylumCase).clear(WITNESS_6);
        verify(asylumCase).clear(WITNESS_7);
        verify(asylumCase).clear(WITNESS_8);
        verify(asylumCase).clear(WITNESS_9);
        verify(asylumCase).clear(WITNESS_10);
    }

    /*
    If witness1 isn't deleted, witness2 is deleted, witness3 isn't deleted; and interpreters are needed
    for witnesses, it should filter out the deleted witnesses and compress the list with reindexed idValues,
    so that witness1 remains witness1 and witness3 becomes witness2 (with idValue with id:2), and do a similar
    process with the witness related fields (witnessInterpreterLanguageCategory, witnessSpokenLanguageInterpreter,
    witnessSignLanguageInterpreter)
     */
    @Test
    void should_transpose_the_deleted_witnesses_and_related_fields_if_interpreters_needed() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(oldAsylumCase);
        when(witnessDetails1.getWitnessName()).thenReturn("A");
        when(witnessDetails1.getWitnessFamilyName()).thenReturn("a");
        when(witnessDetails1.buildWitnessFullName()).thenReturn("A a");
        when(witnessDetails1.getWitnessPartyId()).thenReturn("partyId1");
        when(witnessDetails2.getWitnessName()).thenReturn("B");
        when(witnessDetails2.getWitnessFamilyName()).thenReturn("b");
        when(witnessDetails2.buildWitnessFullName()).thenReturn("B b");
        when(witnessDetails2.getIsWitnessDeleted()).thenReturn(YES);
        when(witnessDetails2.getWitnessPartyId()).thenReturn("partyId2");
        when(witnessDetails3.getWitnessName()).thenReturn("C");
        when(witnessDetails3.getWitnessFamilyName()).thenReturn("c");
        when(witnessDetails3.buildWitnessFullName()).thenReturn("C c");
        when(witnessDetails3.getWitnessPartyId()).thenReturn("partyId3");

        List<IdValue<WitnessDetails>> twoOldWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2));

        List<IdValue<WitnessDetails>> twoCurrentWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("3", witnessDetails3));

        // witnessDetails1 appears in new list: it wasn't deleted
        // witnessDetails2 doesn't appear in new list: it was deleted
        // witnessDetails3 only appears in new list: it was added anew

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoCurrentWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(twoOldWitnesses));

        when(asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(asylumCase.read(WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER)));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.ofNullable(interpreterLanguage));

        when(asylumCase.read(WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SIGN_LANGUAGE_INTERPRETER)));
        when(asylumCase.read(WITNESS_3_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.ofNullable(interpreterLanguage2));
        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY, List.of(SPOKEN_LANGUAGE_INTERPRETER));
        verify(asylumCase).write(WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY, List.of(SIGN_LANGUAGE_INTERPRETER));
        verify(asylumCase).write(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, interpreterLanguage);
        verify(asylumCase).write(WITNESS_2_INTERPRETER_SIGN_LANGUAGE, interpreterLanguage2);

        ArgumentCaptor<WitnessDetails> witnessDetailsCaptor = ArgumentCaptor.forClass(WitnessDetails.class);
        verify(asylumCase).write(eq(WITNESS_1), witnessDetailsCaptor.capture());
        verify(asylumCase).write(eq(WITNESS_2), witnessDetailsCaptor.capture());
        assertEquals("A a", witnessDetailsCaptor.getAllValues().get(0).buildWitnessFullName());
        assertEquals("C c", witnessDetailsCaptor.getAllValues().get(1).buildWitnessFullName());

        ArgumentCaptor<List<IdValue<WitnessDetails>>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(asylumCase).write(eq(WITNESS_DETAILS), anyList());

        verify(asylumCase).write(WITNESS_COUNT, 2);
        verify(asylumCase).clear(WITNESS_1_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_3);
        verify(asylumCase).clear(WITNESS_3_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_4);
        verify(asylumCase).clear(WITNESS_4_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_5);
        verify(asylumCase).clear(WITNESS_5_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_6);
        verify(asylumCase).clear(WITNESS_6_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_7);
        verify(asylumCase).clear(WITNESS_7_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_8);
        verify(asylumCase).clear(WITNESS_8_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_9);
        verify(asylumCase).clear(WITNESS_9_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_10);
        verify(asylumCase).clear(WITNESS_10_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE);
    }

    @Test
    void should_clear_dates_to_avoid_if_dates_to_avoid_yes_no_is_no() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).clear(DATES_TO_AVOID);
    }

    @Test
    void should_clear_dates_to_avoid_if_dates_to_avoid_yes_no_is_empty() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.empty());

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).clear(DATES_TO_AVOID);
    }

    @Test
    void should_not_clear_date_to_avoid_if_date_to_avoid_yes_no_is_yes() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DATES_TO_AVOID_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).clear(DATES_TO_AVOID);
    }

    @Test
    void should_clear_all_witness_related_fields_if_no_witness_attending() {

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(Arrays
            .asList(new IdValue("1", new WitnessDetails("cap", "cap")),
                new IdValue("2", new WitnessDetails("Pan", "Pan")))));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(NO));

        updateHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        WITNESS_N_FIELD.forEach(field -> verify(asylumCase, times(1)).clear(field));
        WITNESS_N_INTERPRETER_CATEGORY_FIELD.forEach(field -> verify(asylumCase, times(1))
            .write(field, Collections.emptyList()));
        WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.forEach(field -> verify(asylumCase, times(1)).clear(field));
        WITNESS_N_INTERPRETER_SIGN_LANGUAGE.forEach(field -> verify(asylumCase, times(1)).clear(field));
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
}
