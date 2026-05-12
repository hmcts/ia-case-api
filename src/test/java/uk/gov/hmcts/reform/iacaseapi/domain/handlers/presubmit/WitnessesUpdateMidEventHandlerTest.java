package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ANY_WITNESS_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_10;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_3_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_5;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_6;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_7;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_8;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_9;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessInterpreterLanguagesDynamicListUpdater.SIGN_LANGUAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessesUpdateMidEventHandler.SIGN;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.WitnessesUpdateMidEventHandler.SPOKEN;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
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
    private static final String WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID = "whichWitnessRequiresInterpreter";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String PARTY_ID_1 = "partyId1";
    private static final String PARTY_ID_2 = "partyId2";
    private static final String PARTY_ID_3 = "partyId3";
    private static final String PARTY_ID_A = "partyIdA";
    private static final String PARTY_ID_B = "partyIdB";

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
        when(witnessDetails1.getIsWitnessDeleted()).thenReturn(NO);
        when(witnessDetails1.getWitnessPartyId()).thenReturn(PARTY_ID_1);
        String witnessName2 = "2";
        when(witnessDetails2.getWitnessName()).thenReturn(witnessName2);
        String witnessFamilyName2 = "2";
        when(witnessDetails2.getWitnessFamilyName()).thenReturn(witnessFamilyName2);
        when(witnessDetails2.buildWitnessFullName()).thenReturn(witnessName2 + " " + witnessFamilyName2);
        when(witnessDetails2.getIsWitnessDeleted()).thenReturn(NO);
        when(witnessDetails2.getWitnessPartyId()).thenReturn(PARTY_ID_2);
        String witnessName3 = "3";
        when(witnessDetails3.getWitnessName()).thenReturn(witnessName3);
        String witnessFamilyName3 = "3";
        when(witnessDetails3.getWitnessFamilyName()).thenReturn(witnessFamilyName3);
        when(witnessDetails3.buildWitnessFullName()).thenReturn(witnessName3 + " " + witnessFamilyName3);
        when(witnessDetails3.getIsWitnessDeleted()).thenReturn(NO);
        when(witnessDetails3.getWitnessPartyId()).thenReturn(PARTY_ID_3);
        String witnessNameA = "A";
        when(witnessDetailsA.getWitnessName()).thenReturn(witnessNameA);
        String witnessFamilyNameA = "A";
        when(witnessDetailsA.getWitnessFamilyName()).thenReturn(witnessFamilyNameA);
        when(witnessDetailsA.buildWitnessFullName()).thenReturn(witnessNameA + " " + witnessFamilyNameA);
        when(witnessDetailsA.getWitnessPartyId()).thenReturn(PARTY_ID_A);
        String witnessNameB = "B";
        when(witnessDetailsB.getWitnessName()).thenReturn(witnessNameB);
        String witnessFamilyNameB = "B";
        when(witnessDetailsB.getWitnessFamilyName()).thenReturn(witnessFamilyNameB);
        when(witnessDetailsB.buildWitnessFullName()).thenReturn(witnessNameB + " " + witnessFamilyNameB);
        when(witnessDetailsB.getWitnessPartyId()).thenReturn(PARTY_ID_B);

    }

    @AfterEach
    public void close() {
        interpreterLanguagesUtils.close();
    }

    @Test
    void should_add_error_when_witnesses_more_than_ten() {
        List<IdValue<WitnessDetails>> oneDeletedWitnesses = List.of(new IdValue<>("1", witnessDetails1));
        when(witnessDetails1.getWitnessPartyId()).thenReturn("partyId1");
        when(witnessDetails1.getIsWitnessDeleted()).thenReturn(YES);
        when(witnessDetails2.getWitnessPartyId()).thenReturn("partyId2");
        List<IdValue<WitnessDetails>> tenNonDeletedWitnesses = Collections
            .nCopies(10, new IdValue<>("1", witnessDetails2));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(IS_WITNESSES_ATTENDING_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(oneDeletedWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(tenNonDeletedWitnesses));
        // inclusiveWitnessList will contain 1 deleted witness + 10 new witnesses (11 in total)

        PreSubmitCallbackResponse<AsylumCase> response = witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(
            "Total number of witnesses being handled cannot be higher than 10. "
            + "Number of removed witnesses: 1. Number of active witnesses: 10. "
            + "It's advised to only remove witnesses in this update and then add "
            + "new ones in another update."));
    }

    @Test
    void should_not_add_error_when_witnesses_are_ten_or_less() {
        List<IdValue<WitnessDetails>> oneDeletedWitnesses = List.of(new IdValue<>("1", witnessDetails1));
        when(witnessDetails1.getIsWitnessDeleted()).thenReturn(YES);
        List<IdValue<WitnessDetails>> tenNonDeletedWitnesses = Collections
            .nCopies(9, new IdValue<>("1", witnessDetails2));

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn(IS_WITNESSES_ATTENDING_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(oneDeletedWitnesses));
        when(oldAsylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(tenNonDeletedWitnesses));
        // inclusiveWitnessList will contain 1 deleted witness + 9 new witnesses (10 in total)

        PreSubmitCallbackResponse<AsylumCase> response = witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_write_onto_witness_n_individual_fields_if_witness_interpreter_required() {
        when(callback.getPageId()).thenReturn(IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID);
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

        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        ArgumentCaptor<WitnessDetails> witnessDetailsCaptor = ArgumentCaptor.forClass(WitnessDetails.class);
        verify(asylumCase).write(eq(WITNESS_1), witnessDetailsCaptor.capture());
        verify(asylumCase).write(eq(WITNESS_2), witnessDetailsCaptor.capture());
        verify(asylumCase).write(eq(WITNESS_3), witnessDetailsCaptor.capture());
        assertEquals("1 1", witnessDetailsCaptor.getAllValues().get(0).buildWitnessFullName());
        assertEquals(NO, witnessDetailsCaptor.getAllValues().get(0).getIsWitnessDeleted());
        assertEquals("2 2", witnessDetailsCaptor.getAllValues().get(1).buildWitnessFullName());
        assertEquals(NO, witnessDetailsCaptor.getAllValues().get(1).getIsWitnessDeleted());
        assertEquals("3 3", witnessDetailsCaptor.getAllValues().get(2).buildWitnessFullName());
        assertEquals(NO, witnessDetailsCaptor.getAllValues().get(2).getIsWitnessDeleted());
        verify(asylumCase).clear(WITNESS_4);
        verify(asylumCase).clear(WITNESS_5);
        verify(asylumCase).clear(WITNESS_6);
        verify(asylumCase).clear(WITNESS_7);
        verify(asylumCase).clear(WITNESS_8);
        verify(asylumCase).clear(WITNESS_9);
        verify(asylumCase).clear(WITNESS_10);
    }

    @Test
    void should_not_write_fields_if_witness_interpreter_not_required() {
        when(callback.getPageId()).thenReturn(IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID);
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

        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        verify(asylumCase, never()).write(eq(WITNESS_1), any(WitnessDetails.class));
        verify(asylumCase, never()).write(eq(WITNESS_2), any(WitnessDetails.class));
        verify(asylumCase, never()).write(eq(WITNESS_3), any(WitnessDetails.class));
    }

    @Test
    void should_set_up_forthcoming_fields_if_needed_and_not_pre_populated() {
        when(callback.getPageId()).thenReturn(WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID);
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

        when(asylumCase.<List<String>>read(WITNESS_1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(
            Optional.of(List.of(SPOKEN, SIGN))
        );
        when(asylumCase.<List<String>>read(WITNESS_3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(
            Optional.of(List.of(SIGN))
        );

        when(oldAsylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(spokenLanguages));

        /*
        WITNESS 1: not deleted, used to have Spoken Language, now needs Spoken + Sign language
        WITNESS 2: deleted
        WITNESS 3: newly added witness, needs Spoken language
         */

        when(witnessInterpreterLanguagesDynamicListUpdater.generateDynamicList(SIGN_LANGUAGES))
            .thenReturn(signLanguages);
        witnessesUpdateMidEventHandler.handle(MID_EVENT, callback);

        verify(asylumCase).write(eq(WITNESS_DETAILS), anyList()); // WitnessService for witness partyIds
        verify(asylumCase, never()).write(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(asylumCase, times(1)).write(WITNESS_1_INTERPRETER_SIGN_LANGUAGE, signLanguages);
        verify(asylumCase, times(1)).write(WITNESS_3_INTERPRETER_SIGN_LANGUAGE, signLanguages);
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
                        WHICH_WITNESS_REQUIRES_INTERPRETER_PAGE_ID,
                        IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID).contains(callback.getPageId())) {

                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }

                reset(callback);
            }
        }
    }

    @Test
    void should_read_spoken_language_field_when_category_is_spoken() {
        int index = 0;
        InterpreterLanguageRefData spokenLanguage = new InterpreterLanguageRefData();
        spokenLanguage.setLanguageManualEntry(Collections.singletonList("Yes"));
        when(oldAsylumCase.read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index), InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(spokenLanguage));

        boolean result = witnessesUpdateMidEventHandler.existingSelectionWasManual(oldAsylumCase, index, WitnessesUpdateMidEventHandler.SPOKEN);

        assertTrue(result);
        verify(oldAsylumCase).read(WITNESS_N_INTERPRETER_SPOKEN_LANGUAGE.get(index), InterpreterLanguageRefData.class);
    }

    @Test
    void should_read_sign_language_field_when_category_is_sign() {
        int index = 1;
        InterpreterLanguageRefData signLanguage = new InterpreterLanguageRefData();
        signLanguage.setLanguageManualEntry(Collections.singletonList("Yes"));
        when(oldAsylumCase.read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index), InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(signLanguage));

        boolean result = witnessesUpdateMidEventHandler.existingSelectionWasManual(oldAsylumCase, index, WitnessesUpdateMidEventHandler.SIGN);

        assertTrue(result);
        verify(oldAsylumCase).read(WITNESS_N_INTERPRETER_SIGN_LANGUAGE.get(index), InterpreterLanguageRefData.class);
    }
}


