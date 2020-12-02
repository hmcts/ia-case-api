package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
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
class ChangeDirectionDueDateActionAvailableUpdaterTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ChangeDirectionDueDateActionAvailableUpdater changeDirectionDueDateActionAvailableUpdater =
        new ChangeDirectionDueDateActionAvailableUpdater();

    @Test
    void should_set_action_available_flag_to_yes_when_state_applies_and_directions_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(DIRECTIONS)).thenReturn(
                Optional.of(
                    Arrays.asList(
                        new IdValue<>("1", mock(Direction.class))
                    )
                )
            );

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                changeDirectionDueDateActionAvailableUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            if (Arrays.asList(
                State.APPEAL_SUBMITTED,
                State.APPEAL_SUBMITTED_OUT_OF_TIME,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.RESPONDENT_REVIEW,
                State.SUBMIT_HEARING_REQUIREMENTS,
                State.LISTING,
                State.PREPARE_FOR_HEARING,
                State.FINAL_BUNDLING,
                State.PRE_HEARING
            ).contains(state)) {

                verify(asylumCase, times(1)).write(CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE, YesOrNo.YES);
            } else {
                verify(asylumCase, times(1)).write(CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE, YesOrNo.NO);
            }

            reset(asylumCase);
        }
    }

    @Test
    void should_set_not_action_available_flag_to_yes_when_there_are_no_directions() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(Collections.emptyList()));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                changeDirectionDueDateActionAvailableUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(1)).write(CHANGE_DIRECTION_DUE_DATE_ACTION_AVAILABLE, YesOrNo.NO);

            reset(asylumCase);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> changeDirectionDueDateActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueDateActionAvailableUpdater.canHandle(callbackStage, callback);

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

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> changeDirectionDueDateActionAvailableUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> changeDirectionDueDateActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
