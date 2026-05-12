package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GENERATE_LIST_CMR_TASK_REQUESTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_LIST_CMR_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
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
    void should_throw_error_if_appeal_type_not_present() {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("AppealType is not present.");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"PA", "RP"})
    void should_set_generate_list_cmr_task_requested_flag_to_yes_for_pa_rp_appeals_with_success_status(AppealType appealType) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1)).write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"DC", "EA", "HU", "AG"})
    void should_set_generate_list_cmr_task_requested_flag_to_yes_for_other_appeal_types_regardless_of_search_status(AppealType appealType) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        // HOME_OFFICE_SEARCH_STATUS is not stubbed because these appeal types bypass Home Office validation

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1)).write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"PA", "RP"})
    void should_return_error_for_pa_rp_appeals_when_home_office_search_status_is_empty(AppealType appealType) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("You need to match the appellant details before you can generate the list CMR task.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"FAIL", "MULTIPLE"})
    void should_return_error_for_pa_appeals_when_home_office_search_status_failed(String searchStatus) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of(searchStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("You need to match the appellant details before you can generate the list CMR task.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"FAIL", "MULTIPLE"})
    void should_return_error_for_rp_appeals_when_home_office_search_status_failed(String searchStatus) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of(searchStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("You need to match the appellant details before you can generate the list CMR task.");
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT", "ABOUT_TO_START"})
    void should_handle_valid_callback_combinations(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);

        boolean canHandle = generateListCmrTaskHandler.canHandle(callbackStage, callback);

        assertTrue(canHandle);
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"MID_EVENT"})
    void should_not_handle_generate_list_cmr_task_with_invalid_callback_stages(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);

        boolean canHandle = generateListCmrTaskHandler.canHandle(callbackStage, callback);

        assertFalse(canHandle);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"GENERATE_LIST_CMR_TASK"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_other_events_with_valid_stages(Event event) {
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle1 = generateListCmrTaskHandler.canHandle(ABOUT_TO_SUBMIT, callback);
        boolean canHandle2 = generateListCmrTaskHandler.canHandle(ABOUT_TO_START, callback);

        assertFalse(canHandle1);
        assertFalse(canHandle2);
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
                Arguments.of(Event.START_APPEAL, ABOUT_TO_START),
                Arguments.of(Event.SUBMIT_APPEAL, PreSubmitCallbackStage.MID_EVENT),
                Arguments.of(Event.LIST_CASE, ABOUT_TO_START),
                Arguments.of(Event.SEND_DIRECTION, PreSubmitCallbackStage.MID_EVENT),
                Arguments.of(Event.REQUEST_RESPONDENT_EVIDENCE, ABOUT_TO_START),
                Arguments.of(Event.GENERATE_LIST_CMR_TASK, PreSubmitCallbackStage.MID_EVENT)
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
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT", "ABOUT_TO_START"}, mode = EnumSource.Mode.EXCLUDE)
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

        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_error_when_cannot_handle_callback() {
        assertThatThrownBy(() -> generateListCmrTaskHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"PA", "RP"})
    void should_validate_successfully_for_pa_rp_appeals_with_success_status_about_to_start(AppealType appealType) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertThat(callbackResponse.getErrors()).isEmpty();

        // Flag should NOT be written for ABOUT_TO_START stage (validation only)
        verify(asylumCase, times(0)).write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"PA", "RP"})
    void should_return_error_for_pa_rp_appeals_when_home_office_search_status_is_empty_about_to_start(AppealType appealType) {
        when(callback.getEvent()).thenReturn(GENERATE_LIST_CMR_TASK);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateListCmrTaskHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("You need to match the appellant details before you can generate the list CMR task.");
        
        // Flag should NOT be written for ABOUT_TO_START stage, especially when there's an error
        verify(asylumCase, times(0)).write(GENERATE_LIST_CMR_TASK_REQUESTED, YES);
    }
} 