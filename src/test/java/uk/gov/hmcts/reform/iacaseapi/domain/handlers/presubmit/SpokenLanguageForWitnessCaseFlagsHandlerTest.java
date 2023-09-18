package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SpokenLanguageForWitnessCaseFlagsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider systemDateProvider;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;
    @Mock
    private WitnessDetails witnessDetails1;
    @Mock
    private WitnessDetails witnessDetails2;

    private SpokenLanguageForWitnessCaseFlagsHandler handler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(systemDateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        witnessDetails = Arrays.asList(
                new IdValue<>("1", new WitnessDetails("1234", "Witness1Given", "Witness1Family")),
                new IdValue<>("2", new WitnessDetails("2333","Witness2Given", "Witness2Family"))
        );
        handler =
                new SpokenLanguageForWitnessCaseFlagsHandler(systemDateProvider);
    }

    private InterpreterLanguageRefData interpreterLanguageRefDataMocked(boolean manualEntry) {
        if (manualEntry) {
            List<String> list = new ArrayList<>();
            list.add("Spanish");
            return new InterpreterLanguageRefData(null, list, "test description");
        }
        return new InterpreterLanguageRefData(new DynamicList("SPN"), null, null);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "REVIEW_HEARING_REQUIREMENTS",
            "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_interpreter_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));
        when(asylumCase.read(WITNESS_2)).thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family")));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefDataMocked(true)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "REVIEW_HEARING_REQUIREMENTS",
            "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_interpreter_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @Test
    void should_deactivate_interpreter_language_flag() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
                .name(INTERPRETER_LANGUAGE_FLAG.getName())
                .status("Active")
                .build()));

        StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                "Witness1Given", StrategicCaseFlag.ROLE_ON_CASE_WITNESS, existingFlags);

        List<PartyFlagIdValue> list = new ArrayList<>();
        list.add(new PartyFlagIdValue("1234", caseFlag));

        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(list));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Optional<List<PartyFlagIdValue>> witnessFlagsResponse = callbackResponse.getData().read(WITNESS_LEVEL_FLAGS);
        assertTrue(hasInactiveFlag(witnessFlagsResponse.get()));

        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    boolean hasInactiveFlag(List<PartyFlagIdValue> partyFlagList){
        boolean isInactive = false;
        for (PartyFlagIdValue idValue : partyFlagList){
            isInactive = idValue.getValue().getDetails()
                    .stream()
                    .anyMatch(caseFlag -> caseFlag.getCaseFlagValue().getStatus().equals("Inactive"));
            if (isInactive) break;
        }
        return isInactive;
    }

    @Test
    void should_not_deactivate_flag_when_non_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_flag_when_an_inactive_one_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
                .name(INTERPRETER_LANGUAGE_FLAG.getName())
                .status("Inactive")
                .build()));

        StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                "Witness1Given", StrategicCaseFlag.ROLE_ON_CASE_WITNESS, existingFlags);

        List<PartyFlagIdValue> list = new ArrayList<>();
        list.add(new PartyFlagIdValue("1234", caseFlag));

        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(list));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Optional<List<PartyFlagIdValue>> witnessFlagsResponse = callbackResponse.getData().read(WITNESS_LEVEL_FLAGS);
        assertTrue(hasInactiveFlag(witnessFlagsResponse.get()));

        verify(asylumCase, times(0)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "REVIEW_HEARING_REQUIREMENTS",
            "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_flag_when_an_inactive_one_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));
        when(asylumCase.read(WITNESS_2)).thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family")));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefDataMocked(true)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
                .name(INTERPRETER_LANGUAGE_FLAG.getName().concat(" " + "Spanish"))
                .status("Inactive")
                .build()));

        StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                "Witness1Given", StrategicCaseFlag.ROLE_ON_CASE_WITNESS, existingFlags);

        List<PartyFlagIdValue> list = new ArrayList<>();
        list.add(new PartyFlagIdValue("1234", caseFlag));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(list));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "REVIEW_HEARING_REQUIREMENTS",
            "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_flag_when_an_active_one_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_ANY_WITNESS_INTERPRETER_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family")));
        when(asylumCase.read(WITNESS_2)).thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family")));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)).thenReturn(Optional.of(interpreterLanguageRefDataMocked(true)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
                .name(INTERPRETER_LANGUAGE_FLAG.getName().concat("" + "Spanish"))
                .status("Active")
                .build()));

        StrategicCaseFlag caseFlag = new StrategicCaseFlag(
                "Witness1Given", StrategicCaseFlag.ROLE_ON_CASE_WITNESS, existingFlags);

        List<PartyFlagIdValue> list = new ArrayList<>();
        list.add(new PartyFlagIdValue("1234", caseFlag));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS)).thenReturn(Optional.of(list));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && List.of(REVIEW_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS)
                        .contains(callback.getEvent())) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }


}
