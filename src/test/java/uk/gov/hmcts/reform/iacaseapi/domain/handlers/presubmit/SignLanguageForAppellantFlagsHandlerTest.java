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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SIGN_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.INACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SignLanguageForAppellantFlagsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider systemDateProvider;

    private SignLanguageForAppellantFlagsHandler signLanguageHandler;
    private final String appellantDisplayName = "Eke Uke";
    private final Value signLanguageValue = new Value("sign-lps", "Lipspeaker");

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(systemDateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        signLanguageHandler =
                new SignLanguageForAppellantFlagsHandler(systemDateProvider);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_sign_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_SIGN_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_sign_language_flag_for_language_entered_manually(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_SIGN_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(true)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_sign_language_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_deactivate_sign_language_flag() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(SIGN_LANGUAGE.getFlagCode())
                .name(buildLanguageFlagName(SIGN_LANGUAGE.getName(), signLanguageValue.getLabel()))
                .status("Active")
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(
                        appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_flag_when_non_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_flag_when_an_inactive_one_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(SIGN_LANGUAGE.getFlagCode())
                .name(buildLanguageFlagName(SIGN_LANGUAGE.getName(), signLanguageValue.getLabel()))
                .status(INACTIVE_STATUS)
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(
                        appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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
        when(asylumCase.read(IS_SIGN_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(true)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(SIGN_LANGUAGE.getFlagCode())
                .name(buildLanguageFlagName(SIGN_LANGUAGE.getName(), signLanguageValue.getLabel()))
                .status(INACTIVE_STATUS)
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(
                        appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_flag_when_an_active_one_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_SIGN_SERVICES_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false)));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(SIGN_LANGUAGE.getFlagCode())
                .name(buildLanguageFlagName(SIGN_LANGUAGE.getName(), signLanguageValue.getLabel()))
                .status("Active")
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(
                        appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = signLanguageHandler.canHandle(callbackStage, callback);

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
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.of(interpreterLanguageRefDataMocked(false)));

        assertThatThrownBy(() ->
                signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Appellant given names required")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> signLanguageHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> signLanguageHandler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> signLanguageHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> signLanguageHandler.handle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> signLanguageHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> signLanguageHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    private InterpreterLanguageRefData interpreterLanguageRefDataMocked(boolean manualEntry) {
        if (manualEntry) {
            List<String> list = new ArrayList<>();
            list.add(signLanguageValue.getLabel());
            return new InterpreterLanguageRefData(null, list, signLanguageValue.getLabel());
        }
        DynamicList dynamicList = new DynamicList("");
        dynamicList.setValue(signLanguageValue);
        return new InterpreterLanguageRefData(dynamicList, null, null);
    }

    private String buildLanguageFlagName(String flagName, String language) {
        return flagName + " " + language;
    }

}
