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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IN_CAMERA_COURT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LOOP_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_ROOM_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_IN_CAMERA_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_MULTIMEDIA_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MULTIMEDIA_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.AUDIO_VIDEO_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.CASE_GIVEN_IN_PRIVATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HearingRequirementsAppellantFlagsHandler.CASE_GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HearingRequirementsAppellantFlagsHandler.CASE_REFUSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.INACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ROLE_ON_CASE_APPELLANT;

import java.time.LocalDateTime;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingRequirementsAppellantFlagsHandlerTest {

    @Captor
    ArgumentCaptor<StrategicCaseFlag> caseFlagArgumentCaptor;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider systemDateProvider;

    private HearingRequirementsAppellantFlagsHandler hearingRequirementsAppellantCaseFlagsHandler;
    private final String appellantDisplayName = "Eke Uke";

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of(appellantDisplayName));
        when(systemDateProvider.nowWithTime()).thenReturn(LocalDateTime.now());

        hearingRequirementsAppellantCaseFlagsHandler =
            new HearingRequirementsAppellantFlagsHandler(systemDateProvider);
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

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(ACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(HEARING_LOOP.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
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

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(ACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
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

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());
        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();

        assertNotNull(caseFlag);
        assertEquals(2, caseFlag.getDetails().size());
        assertEquals(ACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(ACTIVE_STATUS, caseFlag.getDetails().get(1).getValue().getStatus());

        List<String> actualFlagCodes = List.of(
            caseFlag.getDetails().get(0).getValue().getFlagCode(),
            caseFlag.getDetails().get(1).getValue().getFlagCode());

        assertTrue(actualFlagCodes.contains(HEARING_LOOP.getFlagCode()));
        assertTrue(actualFlagCodes.contains(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode()));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_ADJUSTMENTS"
    })
    void should_set_evidence_in_private_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_IN_CAMERA_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(CASE_GRANTED));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
            .thenReturn(Optional.of(appellantDisplayName));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(ACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(CASE_GIVEN_IN_PRIVATE.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_deactivate_hearing_loop_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_LOOP_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(INACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(HEARING_LOOP.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_deactivate_hearing_loop_flag(Event event) {
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
    void should_deactivate_wheel_chair_access_flag(Event event) {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUIREMENTS);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode())
            .name(STEP_FREE_WHEELCHAIR_ACCESS.getName())
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(INACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_ADJUSTMENTS",
        "REVIEW_HEARING_REQUIREMENTS"
    })
    void should_deactivate_evidence_in_private_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IN_CAMERA_COURT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_IN_CAMERA_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(CASE_REFUSED));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(CASE_GIVEN_IN_PRIVATE.getFlagCode())
            .name(CASE_GIVEN_IN_PRIVATE.getName())
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(INACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(CASE_GIVEN_IN_PRIVATE.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_ADJUSTMENTS"
    })
    void should_set_audio_video_evidence_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class))
                .thenReturn(Optional.of(CASE_GRANTED));
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class))
                .thenReturn(Optional.of(appellantDisplayName));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
                .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(ACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(AUDIO_VIDEO_EVIDENCE.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_ADJUSTMENTS",
        "REVIEW_HEARING_REQUIREMENTS"
    })
    void should_deactivate_audio_video_evidence_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(MULTIMEDIA_EVIDENCE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_MULTIMEDIA_ALLOWED, String.class))
                .thenReturn(Optional.of(CASE_REFUSED));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(AUDIO_VIDEO_EVIDENCE.getFlagCode())
                .name(AUDIO_VIDEO_EVIDENCE.getName())
                .status(ACTIVE_STATUS)
                .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(
                        appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
                .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(1, caseFlag.getDetails().size());
        assertEquals(INACTIVE_STATUS, caseFlag.getDetails().get(0).getValue().getStatus());
        assertEquals(AUDIO_VIDEO_EVIDENCE.getFlagCode(), caseFlag.getDetails().get(0).getValue().getFlagCode());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_deactivate_wheel_chair_access_flag(Event event) {
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
    void should_not_deactivate_flag_when_non_exists(Event event) {
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
    void should_not_deactivate_flag_when_an_inactive_one_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_HEARING_ROOM_NEEDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
            .builder()
            .flagCode(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode())
            .name(STEP_FREE_WHEELCHAIR_ACCESS.getName())
            .status(INACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

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
            .status(INACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingRequirementsAppellantCaseFlagsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(APPELLANT_LEVEL_FLAGS), caseFlagArgumentCaptor.capture());

        StrategicCaseFlag caseFlag = caseFlagArgumentCaptor.getValue();
        assertNotNull(caseFlag);
        assertEquals(2, caseFlag.getDetails().size());

        CaseFlagDetail activeFlag = caseFlag.getDetails().stream()
            .filter(details -> ACTIVE_STATUS.equals(details.getValue().getStatus())).findAny().orElse(null);
        CaseFlagDetail inactiveFlag = caseFlag.getDetails().stream()
            .filter(details -> INACTIVE_STATUS.equals(details.getValue().getStatus())).findAny().orElse(null);

        assertNotNull(activeFlag);
        assertNotNull(inactiveFlag);
        assertEquals(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode(), activeFlag.getValue().getFlagCode());
        assertEquals(STEP_FREE_WHEELCHAIR_ACCESS.getFlagCode(), inactiveFlag.getValue().getFlagCode());
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
            .status(ACTIVE_STATUS)
            .build()));
        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(new StrategicCaseFlag(
                appellantDisplayName, ROLE_ON_CASE_APPELLANT, existingFlags)));

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
                    && List.of(REVIEW_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS, UPDATE_HEARING_ADJUSTMENTS)
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

        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> hearingRequirementsAppellantCaseFlagsHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}