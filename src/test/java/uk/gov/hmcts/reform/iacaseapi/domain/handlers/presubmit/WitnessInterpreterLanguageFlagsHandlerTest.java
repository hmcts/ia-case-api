package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ANY_WITNESS_INTERPRETER_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.INACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_WITNESS;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WitnessInterpreterLanguageFlagsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider systemDateProvider;
    @Captor
    private ArgumentCaptor<List<PartyFlagIdValue>> partyFlagsCaptor;
    private List<IdValue<WitnessDetails>> witnessDetails;
    private WitnessInterpreterLanguageFlagsHandler handler;
    private final Value spokenLanguageValue = new Value("spa", "Spanish");
    private final Value signLanguageValue = new Value("sign-lps", "Lipspeaker");

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(systemDateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        witnessDetails = Arrays.asList(
                new IdValue<>("1",
                    new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)),
                new IdValue<>("2",
                    new WitnessDetails("2333","Witness2Given", "Witness2Family", NO))
        );
        handler = new WitnessInterpreterLanguageFlagsHandler(systemDateProvider);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_spoken_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));
        when(asylumCase.read(WITNESS_2, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family", NO)));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(true, spokenLanguageValue)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), partyFlagsCaptor.capture());

        List<PartyFlagIdValue> actualWitnessFlags = partyFlagsCaptor.getValue();

        assertEquals(1, actualWitnessFlags.size());
        assertEquals("1234", actualWitnessFlags.get(0).getPartyId());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_deactivate_spoken_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(new PartyFlagIdValue("1234", new StrategicCaseFlag(
                "Witness1Given", ROLE_ON_CASE_WITNESS, existingFlags)))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), partyFlagsCaptor.capture());
        assertTrue(hasInactiveFlag(partyFlagsCaptor.getValue()));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_sign_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));
        when(asylumCase.read(WITNESS_2, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family", NO)));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(true, signLanguageValue)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), partyFlagsCaptor.capture());

        List<PartyFlagIdValue> actualWitnessFlags = partyFlagsCaptor.getValue();

        assertEquals(1, actualWitnessFlags.size());
        assertEquals("1234", actualWitnessFlags.get(0).getPartyId());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_1)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_deactivate_sign_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .name(SIGN_LANGUAGE_INTERPRETER.getName())
            .subTypeKey(signLanguageValue.getCode())
            .subTypeValue(signLanguageValue.getLabel())
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(new PartyFlagIdValue("1234", new StrategicCaseFlag(
                "Witness1Given", ROLE_ON_CASE_WITNESS, existingFlags)))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(eq(WITNESS_LEVEL_FLAGS), partyFlagsCaptor.capture());
        assertTrue(hasInactiveFlag(partyFlagsCaptor.getValue()));
    }

    @Test
    void should_not_deactivate_flag_when_non_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));

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
        when(asylumCase.read(WITNESS_1, WitnessDetails.class)).thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status(INACTIVE_STATUS)
            .build()));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(new PartyFlagIdValue("1234", new StrategicCaseFlag(
                "Witness1Given", ROLE_ON_CASE_WITNESS, existingFlags)))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(eq(WITNESS_LEVEL_FLAGS), any());
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
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));
        when(asylumCase.read(WITNESS_2, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family", NO)));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status(INACTIVE_STATUS)
            .build()));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(new PartyFlagIdValue("1234", new StrategicCaseFlag(
                "Witness1Given", ROLE_ON_CASE_WITNESS, existingFlags)))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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
        when(asylumCase.read(WITNESS_1, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("1234", "Witness1Given", "Witness1Family", NO)));
        when(asylumCase.read(WITNESS_2, WitnessDetails.class))
            .thenReturn(Optional.of(new WitnessDetails("2333","Witness2Given", "Witness2Family", NO)));
        when(asylumCase.read(WITNESS_1_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(WITNESS_LEVEL_FLAGS))
            .thenReturn(Optional.of(List.of(new PartyFlagIdValue("1234", new StrategicCaseFlag(
                "Witness1Given", ROLE_ON_CASE_WITNESS, existingFlags)))));

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
    void cannot_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                assertFalse(handler.canHandle(callbackStage, callback), "Cannot handle event " + event);
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

    private boolean hasInactiveFlag(List<PartyFlagIdValue> partyFlagList) {
        return partyFlagList.stream().map(idValue -> idValue.getValue().getDetails())
            .flatMap(Collection::stream)
            .anyMatch(caseFlag -> caseFlag.getValue().getStatus().equals(INACTIVE_STATUS));
    }

    private InterpreterLanguageRefData interpreterLanguageRefDataMocked(boolean manualEntry, Value value) {
        if (manualEntry) {
            List<String> list = new ArrayList<>();
            list.add("Yes");
            return new InterpreterLanguageRefData(null, list, value.getLabel());
        }
        DynamicList dynamicList = new DynamicList("");
        dynamicList.setValue(value);
        return new InterpreterLanguageRefData(dynamicList, null, null);
    }
}
