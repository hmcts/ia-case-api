package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LOOP_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_ROOM_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDateTime;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingRequirementsAppellantCaseFlagsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider systemDateProvider;

    private HearingRequirementsAppellantCaseFlagsHandler hearingRequirementsAppellantCaseFlagsHandler;
    private final String appellantDisplayName = "Eke Uke";

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(systemDateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        hearingRequirementsAppellantCaseFlagsHandler =
            new HearingRequirementsAppellantCaseFlagsHandler(systemDateProvider);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_hearing_loop_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_hearing_loop_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_wheel_chair_access_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Eke"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Uke"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_set_wheel_chair_access_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_set_hearing_loop_and_wheelchair_access_flags(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_deactivate_hearing_loop_flag() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_hearing_loop_flag() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_deactivate_wheel_chair_access_flag() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode())
            .name(STEP_FREE_WHEELCHAIR_ACCESS.getName())
            .status("Active")
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_wheel_chair_access_flag() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_flag_when_non_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void should_not_deactivate_flag_when_an_inactive_one_exists() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode())
                .name(STEP_FREE_WHEELCHAIR_ACCESS.getName())
                .status("Inactive")
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode())
                .name(STEP_FREE_WHEELCHAIR_ACCESS.getName())
                .status("Inactive")
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

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
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(HEARING_LOOP.getFlagCode())
                .name(HEARING_LOOP.getName())
                .status("Active")
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_LEVEL_FLAGS), any());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingRequirementsAppellantCaseFlagsHandler.canHandle(callbackStage, callback);

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
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() ->
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Appellant full name is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
