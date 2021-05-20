package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CaseBuildingReadyForSubmissionUpdaterTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allLegalRepDocuments;

    private CaseBuildingReadyForSubmissionUpdater caseBuildingReadyForSubmissionUpdater =
        new CaseBuildingReadyForSubmissionUpdater();

    @Test
    void should_set_case_building_ready_for_submission_flag_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(asylumCase.read(CASE_ARGUMENT_AVAILABLE)).thenReturn(Optional.of(true));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            caseBuildingReadyForSubmissionUpdater
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).clear(CASE_BUILDING_READY_FOR_SUBMISSION);
        verify(asylumCase, times(1)).write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.NO);
    }

    @Test
    void should_set_case_building_ready_for_submission_flag_to_no_if_argument_not_uploaded() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(asylumCase.read(CASE_ARGUMENT_AVAILABLE)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            caseBuildingReadyForSubmissionUpdater
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).clear(CASE_BUILDING_READY_FOR_SUBMISSION);
        verify(asylumCase, never()).write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.NO);
    }

    @Test
    void should_clear_case_building_ready_for_submission_flag_when_not_in_case_building_state() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.AWAITING_RESPONDENT_EVIDENCE);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            caseBuildingReadyForSubmissionUpdater
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(CASE_BUILDING_READY_FOR_SUBMISSION);
        verify(asylumCase, never()).write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_BUILDING_READY_FOR_SUBMISSION, YesOrNo.NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> caseBuildingReadyForSubmissionUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = caseBuildingReadyForSubmissionUpdater.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseBuildingReadyForSubmissionUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> caseBuildingReadyForSubmissionUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseBuildingReadyForSubmissionUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> caseBuildingReadyForSubmissionUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
