package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ChangeDirectionDueDateActionAvailableUpdaterTest {

    @Mock private Callback<CaseDataMap> callback;
    @Mock private CaseDetails<CaseDataMap> caseDetails;
    @Mock private CaseDataMap CaseDataMap;

    private ChangeDirectionDueDateActionAvailableUpdater changeDirectionDueDateActionAvailableUpdater =
        new ChangeDirectionDueDateActionAvailableUpdater();

    @Test
    public void should_set_action_available_flag_to_yes_when_state_applies_and_directions_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(CaseDataMap.getDirections()).thenReturn(
                Optional.of(
                    Arrays.asList(
                        new IdValue<>("1", mock(Direction.class))
                    )
                )
            );

            PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
                changeDirectionDueDateActionAvailableUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(CaseDataMap, callbackResponse.getData());

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

                verify(CaseDataMap, times(1)).setChangeDirectionDueDateActionAvailable(YesOrNo.YES);
            } else {
                verify(CaseDataMap, times(1)).setChangeDirectionDueDateActionAvailable(YesOrNo.NO);
            }

            reset(CaseDataMap);
        }
    }

    @Test
    public void should_set_not_action_available_flag_to_yes_when_there_are_no_directions() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(CaseDataMap.getDirections()).thenReturn(Optional.of(Collections.emptyList()));

            PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
                changeDirectionDueDateActionAvailableUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(CaseDataMap, callbackResponse.getData());

            verify(CaseDataMap, times(1)).setChangeDirectionDueDateActionAvailable(YesOrNo.NO);

            reset(CaseDataMap);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback_for_all_events() {

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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
