package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GENERATE_LIST_CMR_TASK_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_LIST_CMR_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateListCmrTaskHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private GenerateListCmrTaskHandler generateListCmrTaskHandler;

    @BeforeEach
    void setUp() {
        generateListCmrTaskHandler = new GenerateListCmrTaskHandler();
    }

    @Test
    void should_set_generate_list_cmr_task_requested_flag_to_yes() {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);
    }

    @Test
    void should_handle_valid_callback_combination() {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);

        boolean canHandle = generateListCmrTaskHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_START", "MID_EVENT"})
    void should_not_handle_generate_list_cmr_task_with_invalid_callback_stages(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);

        boolean canHandle = generateListCmrTaskHandler.canHandle(callbackStage, callback);

        assertFalse(canHandle);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"GENERATE_LIST_CMR_TASK"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_other_events_with_about_to_submit_stage(Event event) {
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = generateListCmrTaskHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @ParameterizedTest
    @MethodSource("invalidEventAndStageCombo")
    void should_not_handle_invalid_event_and_stage_combinations(Event event, PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = generateListCmrTaskHandler.canHandle(stage, callback);

        assertFalse(canHandle);
    }

    private static Stream<Arguments> invalidEventAndStageCombo() {
        return Stream.of(
                Arguments.of(Event.START_APPEAL, PreSubmitCallbackStage.ABOUT_TO_START),
                Arguments.of(Event.SUBMIT_APPEAL, PreSubmitCallbackStage.MID_EVENT),
                Arguments.of(Event.LIST_CASE, PreSubmitCallbackStage.ABOUT_TO_START),
                Arguments.of(Event.SEND_DIRECTION, PreSubmitCallbackStage.MID_EVENT),
                Arguments.of(Event.REQUEST_RESPONDENT_EVIDENCE, PreSubmitCallbackStage.ABOUT_TO_START)
        );
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> generateListCmrTaskHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateListCmrTaskHandler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void should_throw_error_when_handling_with_wrong_callback_stage(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);

        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(callbackStage, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"GENERATE_LIST_CMR_TASK"}, mode = EnumSource.Mode.EXCLUDE)
    void should_throw_error_when_handling_with_wrong_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_error_when_cannot_handle_callback() {
        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
} 