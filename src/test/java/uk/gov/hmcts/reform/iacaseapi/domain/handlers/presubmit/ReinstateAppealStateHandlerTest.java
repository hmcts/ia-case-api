package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RECORD_APPLICATION_ACTION_DISABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REINSTATED_DECISION_MAKER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REINSTATE_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REINSTATE_APPEAL_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATE_BEFORE_END_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REINSTATE_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.RESPONDENT_REVIEW;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReinstateAppealStateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private UserDetails userDetails;
    @Mock private UserDetailsHelper userDetailsDetails;

    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private ReinstateAppealStateHandler reinstateAppealStateHandler;
    private LocalDate date = LocalDate.now();

    @BeforeEach
    public void setup() {
        when(dateProvider.now()).thenReturn(date);
        reinstateAppealStateHandler = new ReinstateAppealStateHandler(dateProvider, userDetails, userDetailsDetails);
    }

    @Test
    void should_set_valid_state_before_end_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(REINSTATE_APPEAL);
        when(asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)).thenReturn(Optional.of(State.RESPONDENT_REVIEW));
        when(asylumCase.read(REINSTATE_APPEAL_REASON)).thenReturn(Optional.of("test"));
        when(userDetailsDetails.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
        verify(asylumCase).write(REINSTATED_DECISION_MAKER, "Legal Officer");
        verify(asylumCase).write(APPEAL_STATUS, AppealStatus.REINSTATED);
        verify(asylumCase).write(REINSTATE_APPEAL_DATE, date.toString());
        verify(asylumCase).write(RECORD_APPLICATION_ACTION_DISABLED, YesOrNo.NO);
        assertTrue(asylumCase.read(REINSTATE_APPEAL_REASON).isPresent());
        assertEquals(RESPONDENT_REVIEW.toString(), returnedCallbackResponse.getState().toString());
    }

    @Test
    void should_return_error_for_state_before_end_appeal_unknown() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(REINSTATE_APPEAL);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
        assertEquals("The appeal cannot be reinstated", returnedCallbackResponse.getErrors().iterator().next());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = reinstateAppealStateHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event == REINSTATE_APPEAL) {

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

        assertThatThrownBy(() -> reinstateAppealStateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reinstateAppealStateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reinstateAppealStateHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
