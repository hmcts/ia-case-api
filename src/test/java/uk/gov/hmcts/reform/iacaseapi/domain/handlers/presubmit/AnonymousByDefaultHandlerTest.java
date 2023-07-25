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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnonymousByDefaultHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AnonymousByDefaultHandler anonymousByDefaultHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        anonymousByDefaultHandler = new AnonymousByDefaultHandler();
    }

    @Test
    void should_set_anonymity_flag_for_PA_appeal() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);
        verify(asylumCase, times(1)).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void should_set_anonymity_flag_for_RP_appeal() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);
        verify(asylumCase, times(1)).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void should_set_anonymity_flag_when_an_inactive_anonymity_flag_exists() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(ANONYMITY.getFlagCode())
                .name(ANONYMITY.getName())
                .status("Inactive")
                .build()));
        when(asylumCase.read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(null, null, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);
        verify(asylumCase, times(1)).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void should_set_anonymity_flag_when_an_empty_class_level_flag_exists() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);
        verify(asylumCase, times(1)).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void anonymity_flag_should_not_be_set_for_non_PA_or_RP_appeal_if_not_already_set() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, never()).read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);
        verify(asylumCase, never()).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void anonymity_flag_should_not_be_set_when_an_active_anonymity_flag_exists() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        List<CaseFlagDetail> existingFlags = List.of(new CaseFlagDetail("123", CaseFlagValue
                .builder()
                .flagCode(ANONYMITY.getFlagCode())
                .name(ANONYMITY.getName())
                .status("Active")
                .build()));
        when(asylumCase.read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class))
                .thenReturn(Optional.of(new StrategicCaseFlag(null, null, existingFlags)));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);
        verify(asylumCase, never()).write(eq(CASE_LEVEL_FLAGS), any());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = anonymousByDefaultHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == SUBMIT_APPEAL) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> anonymousByDefaultHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> anonymousByDefaultHandler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> anonymousByDefaultHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

    }
}
