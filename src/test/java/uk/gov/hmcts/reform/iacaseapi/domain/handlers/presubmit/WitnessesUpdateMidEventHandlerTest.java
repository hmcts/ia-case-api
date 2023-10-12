package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessesUpdateMidEventHandler.SIGN;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.SIGN_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessesUpdateMidEventHandler.SPOKEN;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class WitnessesUpdateMidEventHandlerTest {

    private static final String IS_WITNESSES_ATTENDING_PAGE_ID = "isWitnessesAttending";
    private static final String IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID = "isInterpreterServicesNeeded";
    private static final String APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID = "appellantInterpreterSpokenLanguage";
    private static final String APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID = "appellantInterpreterSignLanguage";
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";

    @Mock
    private WitnessInterpreterLanguagesDynamicListUpdater witnessInterpreterLanguagesDynamicListUpdater;
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
    private WitnessDetails witnessDetailsA;
    @Mock
    private WitnessDetails witnessDetailsB;
    @Mock
    private InterpreterLanguageRefData spokenLanguages;
    @Mock
    private InterpreterLanguageRefData signLanguages;
    @Mock
    private InterpreterLanguageRefData witness1SpokenLanguages;
    @Mock
    private InterpreterLanguageRefData witness1SignLanguages;
    @Mock
    private DynamicMultiSelectList witnessListElement1;
    @Mock
    private DynamicMultiSelectList witnessListElement2;
    @Mock
    private DynamicMultiSelectList witnessListElement3;
    @Mock
    private DynamicMultiSelectList witnessListElement4;

    MockedStatic<InterpreterLanguagesUtils> interpreterLanguagesUtils;

    private WitnessesUpdateMidEventHandler witnessesUpdateMidEventHandler;

    @BeforeEach
    public void setup() {
        interpreterLanguagesUtils = mockStatic(InterpreterLanguagesUtils.class);
        witnessesUpdateMidEventHandler = new WitnessesUpdateMidEventHandler(witnessInterpreterLanguagesDynamicListUpdater);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(oldAsylumCase);
        String witnessName1 = "1";
        when(witnessDetails1.getWitnessName()).thenReturn(witnessName1);
        String witnessFamilyName1 = "1";
        when(witnessDetails1.getWitnessFamilyName()).thenReturn(witnessFamilyName1);
        when(witnessDetails1.buildWitnessFullName()).thenReturn(witnessName1 + " " + witnessFamilyName1);
        String witnessName2 = "2";
        when(witnessDetails2.getWitnessName()).thenReturn(witnessName2);
        String witnessFamilyName2 = "2";
        when(witnessDetails2.getWitnessFamilyName()).thenReturn(witnessFamilyName2);
        when(witnessDetails2.buildWitnessFullName()).thenReturn(witnessName2 + " " + witnessFamilyName2);
        String witnessName3 = "3";
        when(witnessDetails3.getWitnessName()).thenReturn(witnessName3);
        String witnessFamilyName3 = "3";
        when(witnessDetails3.getWitnessFamilyName()).thenReturn(witnessFamilyName3);
        when(witnessDetails3.buildWitnessFullName()).thenReturn(witnessName3 + " " + witnessFamilyName3);
        String witnessNameA = "A";
        when(witnessDetailsA.getWitnessName()).thenReturn(witnessNameA);
        String witnessFamilyNameA = "A";
        when(witnessDetailsA.getWitnessFamilyName()).thenReturn(witnessFamilyNameA);
        when(witnessDetailsA.buildWitnessFullName()).thenReturn(witnessNameA + " " + witnessFamilyNameA);
        String witnessNameB = "B";
        when(witnessDetailsB.getWitnessName()).thenReturn(witnessNameB);
        String witnessFamilyNameB = "B";
        when(witnessDetailsB.getWitnessFamilyName()).thenReturn(witnessFamilyNameB);
        when(witnessDetailsB.buildWitnessFullName()).thenReturn(witnessNameB + " " + witnessFamilyNameB);

    }

    @AfterEach
    public void close() {
        interpreterLanguagesUtils.close();
    }

    @Test
    void should_clear_fields_to_be_cleared_at_end_of_event() {
        witnessesUpdateMidEventHandler.setFieldsToBeCleared(List.of(
            WITNESS_LIST_ELEMENT_1,
            WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE,
            WITNESS_1_INTERPRETER_SIGN_LANGUAGE)
        );

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);

        witnessesUpdateMidEventHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).clear(WITNESS_LIST_ELEMENT_1);
        verify(asylumCase, times(1)).clear(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase, times(1)).clear(WITNESS_1_INTERPRETER_SIGN_LANGUAGE);
    }

    @Test
    void should_add_error_when_witnesses_more_than_ten() {
        List<IdValue<WitnessDetails>> elevenWitnesses = Collections
            .nCopies(11, new IdValue<>("1", witnessDetails1));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(IS_WITNESSES_ATTENDING_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));

        PreSubmitCallbackResponse<AsylumCase> response = witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(WITNESSES_NUMBER_EXCEEDED_ERROR));
    }

    @Test
    void should_not_add_error_when_witnesses_are_ten_or_less() {
        List<WitnessDetails> elevenWitnesses = Collections.nCopies(10, witnessDetails1);

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(IS_WITNESSES_ATTENDING_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));

        PreSubmitCallbackResponse<AsylumCase> response = witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_skip_logic_if_appellant_interpreter_page_is_not_last_page() { // before whichWitnessRequiresInterpterer

        // check if interpreter services are needed to establish whether to break out of the loop or not
        // (i.e. leave it to a further page to run the logic since this wouldn't be the last page before witnesses
        // mappings need to be evaluated)

        List<IdValue<WitnessDetails>> elevenWitnesses = Collections
            .nCopies(10, new IdValue<>("1", witnessDetails1));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));

        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        verify(asylumCase, times(1)).read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class);
    }

    @Test
    void should_skip_logic_if_appellant_spoken_language_page_is_not_last_page() { // before whichWitnessRequiresInterpterer

        // check if logic is skipped (1st method to be called in all the logic is never called)
        // (i.e. leave it to a further page to run the logic since this wouldn't be the last page before witnesses
        // mappings need to be evaluated)

        List<IdValue<WitnessDetails>> elevenWitnesses = Collections
            .nCopies(10, new IdValue<>("1", witnessDetails1));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of(SPOKEN)));

        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        verify(oldAsylumCase, never()).read(WITNESS_LIST_ELEMENT_1, DynamicMultiSelectList.class);
    }

    @Test
    void should_skip_logic_if_appellant_sign_language_page_is_not_last_page() { // before whichWitnessRequiresInterpterer

        // check if logic is skipped (1st method to be called in all the logic is never called)
        // (i.e. leave it to a further page to run the logic since this wouldn't be the last page before witnesses
        // mappings need to be evaluated)

        List<IdValue<WitnessDetails>> elevenWitnesses = Collections
            .nCopies(10, new IdValue<>("1", witnessDetails1));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of(SIGN)));

        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        verify(oldAsylumCase, never()).read(WITNESS_LIST_ELEMENT_1, DynamicMultiSelectList.class);
    }

    @Test
    void should_map_old_to_new_fields() {
        // deleting witness3, adding witnessA, witness

        // OLD WITNESSES
        // witness1 [needs interpreter] SPOKEN + SIGN
        // witness2 [no]
        // witness3 [no]

        // NEW WITNESSES
        // witness1 [needs interpreter] SIGN (should maintain old selection)
        // witness2 [needs interpreter] SPOKEN
        // witnessA [no]
        // witnessB [needs interpreter] SIGN

        List<IdValue<WitnessDetails>> oldWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2),
            new IdValue<>("3", witnessDetails3));

        List<IdValue<WitnessDetails>> newWitnesses = List.of(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2),
            new IdValue<>("A", witnessDetailsA),
            new IdValue<>("B", witnessDetailsB));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(newWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(oldWitnesses));
        when(asylumCase.read(IS_WITNESSES_ATTENDING, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(witnessInterpreterLanguagesDynamicListUpdater.generateDynamicList(INTERPRETER_LANGUAGES))
                .thenReturn(spokenLanguages);
        when(witnessInterpreterLanguagesDynamicListUpdater.generateDynamicList(SIGN_LANGUAGES))
            .thenReturn(signLanguages);

        when(oldAsylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(witness1SpokenLanguages));
        when(oldAsylumCase.read(WITNESS_1_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(witness1SignLanguages));
        when(asylumCase.read(WITNESS_LIST_ELEMENT_1, DynamicMultiSelectList.class)).thenReturn(Optional.of(witnessListElement1));
        when(witnessListElement1.getValue()).thenReturn(List.of(new Value("lang", "lang")));
        when(asylumCase.read(WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of(SIGN)));
        when(asylumCase.read(WITNESS_LIST_ELEMENT_2, DynamicMultiSelectList.class)).thenReturn(Optional.of(witnessListElement2));
        when(witnessListElement2.getValue()).thenReturn(List.of(new Value("lang", "lang")));
        when(asylumCase.read(WITNESS_2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of(SPOKEN)));
        when(asylumCase.read(WITNESS_LIST_ELEMENT_3, DynamicMultiSelectList.class)).thenReturn(Optional.empty());
        when(asylumCase.read(WITNESS_LIST_ELEMENT_4, DynamicMultiSelectList.class)).thenReturn(Optional.of(witnessListElement4));
        when(witnessListElement4.getValue()).thenReturn(List.of(new Value("lang", "lang")));
        when(asylumCase.read(WITNESS_4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of(SIGN)));

        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);


        verify(asylumCase).write(WITNESS_1_INTERPRETER_SIGN_LANGUAGE, witness1SignLanguages);
        verify(asylumCase).write(WITNESS_2_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(asylumCase).write(WITNESS_4_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(asylumCase).clear(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_3_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_4_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_5_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_6_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_7_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_8_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_9_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_10_INTERPRETER_SPOKEN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_2_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_3_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_5_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_6_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_7_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_8_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_9_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).clear(WITNESS_10_INTERPRETER_SIGN_LANGUAGE);
        verify(asylumCase).write(eq(WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));
        verify(asylumCase).write(eq(WITNESS_5_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));
        verify(asylumCase).write(eq(WITNESS_6_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));
        verify(asylumCase).write(eq(WITNESS_7_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));
        verify(asylumCase).write(eq(WITNESS_8_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));
        verify(asylumCase).write(eq(WITNESS_9_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));
        verify(asylumCase).write(eq(WITNESS_10_INTERPRETER_LANGUAGE_CATEGORY), eq(Collections.emptyList()));

        assertEquals(17, witnessesUpdateMidEventHandler.getFieldsToBeCleared().size());
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> witnessesUpdateMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SEND_DIRECTION);
        assertThatThrownBy(
            () -> witnessesUpdateMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        Set<String> pageIds = Set.of(IS_WITNESSES_ATTENDING_PAGE_ID,
            IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID,
            APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID,
            APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID,
            WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID,
            IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID,
            "");

        for (String pageId : pageIds) {
            for (Event event : Event.values()) {

                when(callback.getEvent()).thenReturn(event);
                when(callback.getPageId()).thenReturn(pageId);

                for (PreSubmitCallbackStage callbackStage : values()) {

                    boolean canHandle = witnessesUpdateMidEventHandler.canHandle(callbackStage, callback);

                    if (event.equals(UPDATE_HEARING_REQUIREMENTS)
                        && callbackStage == MID_EVENT
                        && Set.of(IS_WITNESSES_ATTENDING_PAGE_ID,
                        IS_INTERPRETER_SERVICES_NEEDED_PAGE_ID,
                        APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_PAGE_ID,
                        APPELLANT_INTERPRETER_SIGN_LANGUAGE_PAGE_ID,
                        WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID,
                        IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID).contains(callback.getPageId())
                        || (callbackStage == ABOUT_TO_SUBMIT
                            && callback.getEvent().equals(UPDATE_HEARING_REQUIREMENTS))) {

                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }

                reset(callback);
            }
        }
    }
}


