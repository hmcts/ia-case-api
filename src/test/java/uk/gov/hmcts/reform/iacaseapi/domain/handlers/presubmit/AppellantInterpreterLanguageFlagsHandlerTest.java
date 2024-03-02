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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.INACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantInterpreterLanguageFlagsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider systemDateProvider;
    @Captor
    private ArgumentCaptor<StrategicCaseFlag> partyFlagsCaptor;

    private AppellantInterpreterLanguageFlagsHandler handler;
    private final String appellantDisplayName = "Eke Uke";
    private final Value spokenLanguageValue = new Value("spa", "Spanish");
    private final Value signLanguageValue = new Value("sign-lps", "Lipspeaker");

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(systemDateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        handler =
                new AppellantInterpreterLanguageFlagsHandler(systemDateProvider);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_interpreter_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(1, flagDetails.size());
        assertEquals(INTERPRETER_LANGUAGE_FLAG.getFlagCode(), flagDetails.get(0).getValue().getFlagCode());
        assertEquals("Active", flagDetails.get(0).getValue().getStatus());
        assertEquals(spokenLanguageValue.getCode(), flagDetails.get(0).getValue().getSubTypeKey());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_interpreter_language_flag_for_language_entered_manually(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(true, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(1, flagDetails.size());
        assertEquals(INTERPRETER_LANGUAGE_FLAG.getFlagCode(), flagDetails.get(0).getValue().getFlagCode());
        assertEquals("Active", flagDetails.get(0).getValue().getStatus());
        assertEquals(spokenLanguageValue.getLabel(), flagDetails.get(0).getValue().getSubTypeValue());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_interpreter_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_deactivate_interpreter_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(1, flagDetails.size());
        assertEquals(INTERPRETER_LANGUAGE_FLAG.getFlagCode(), flagDetails.get(0).getValue().getFlagCode());
        assertEquals("Inactive", flagDetails.get(0).getValue().getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_sign_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, signLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(1, flagDetails.size());
        assertEquals(StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER.getFlagCode(), flagDetails.get(0).getValue().getFlagCode());
        assertEquals("Active", flagDetails.get(0).getValue().getStatus());
        assertEquals(signLanguageValue.getCode(), flagDetails.get(0).getValue().getSubTypeKey());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_sign_language_flag_for_language_entered_manually(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SIGN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(true, signLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(1, flagDetails.size());
        assertEquals(StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER.getFlagCode(), flagDetails.get(0).getValue().getFlagCode());
        assertEquals("Active", flagDetails.get(0).getValue().getStatus());
        assertEquals(signLanguageValue.getLabel(), flagDetails.get(0).getValue().getSubTypeValue());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_deactivate_sign_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER.getFlagCode())
            .name(StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER.getName())
            .subTypeKey(signLanguageValue.getCode())
            .subTypeValue(signLanguageValue.getLabel())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(1, flagDetails.size());
        assertEquals(StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER.getFlagCode(), flagDetails.get(0).getValue().getFlagCode());
        assertEquals("Inactive", flagDetails.get(0).getValue().getStatus());
        assertEquals(signLanguageValue.getCode(), flagDetails.get(0).getValue().getSubTypeKey());
    }

    @Test
    void should_not_deactivate_flag_when_non_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_flag_when_an_inactive_one_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status(INACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_flag_when_an_inactive_one_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status(INACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), partyFlagsCaptor.capture());

        StrategicCaseFlag appellantLanguageFlag = partyFlagsCaptor.getValue();
        List<CaseFlagDetail> flagDetails = appellantLanguageFlag.getDetails();
        assertEquals(2, flagDetails.size());
        CaseFlagDetail activeFlagDetail = flagDetails.stream()
            .filter(details -> Objects.equals(details.getValue().getStatus(), "Active"))
            .findAny().orElse(null);
        CaseFlagDetail inactiveFlagDetail = flagDetails.stream()
            .filter(details -> Objects.equals(details.getValue().getStatus(), "Inactive"))
            .findAny().orElse(null);
        assertNotNull(activeFlagDetail);
        assertNotNull(inactiveFlagDetail);
        assertEquals(activeFlagDetail.getValue().getFlagCode(), inactiveFlagDetail.getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_flag_when_an_active_one_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue())));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        
        
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey(spokenLanguageValue.getCode())
            .subTypeValue(spokenLanguageValue.getLabel())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                    appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_throw_exception_when_appellant_name_is_missing(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false, spokenLanguageValue)));

        assertThatThrownBy(() ->
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Appellant given names required")
                .isExactlyInstanceOf(IllegalStateException.class);
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
