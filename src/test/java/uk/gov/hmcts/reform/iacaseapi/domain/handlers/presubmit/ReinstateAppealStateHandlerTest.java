package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REINSTATE_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.RESPONDENT_REVIEW;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ReinstateAppealStateHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;
    @Mock private FeatureToggler featureToggler;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private ReinstateAppealStateHandler reinstateAppealStateHandler;
    private LocalDate date = LocalDate.now();

    @Before
    public void setup() {
        when(dateProvider.now()).thenReturn(date);
        reinstateAppealStateHandler = new ReinstateAppealStateHandler(featureToggler, dateProvider, userDetailsProvider);
        when(featureToggler.getValue("reinstate-feature", false)).thenReturn(true);
    }

    @Test
    public void should_set_valid_state_before_end_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(REINSTATE_APPEAL);
        when(asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)).thenReturn(Optional.of(State.RESPONDENT_REVIEW));
        when(asylumCase.read(REINSTATE_APPEAL_REASON)).thenReturn(Optional.of("test"));
        when(userDetailsProvider.getLoggedInUserRoleLabel()).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> returnedCallbackResponse =
            reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertNotNull(returnedCallbackResponse);
        assertEquals(asylumCase, returnedCallbackResponse.getData());
        verify(asylumCase).write(REINSTATED_DECISION_MAKER, UserRoleLabel.TRIBUNAL_CASEWORKER.toString());
        verify(asylumCase).write(APPEAL_STATUS, AppealStatus.REINSTATED);
        verify(asylumCase).write(REINSTATE_APPEAL_DATE, date.toString());
        verify(asylumCase).write(RECORD_APPLICATION_ACTION_DISABLED, YesOrNo.NO);
        assertTrue(asylumCase.read(REINSTATE_APPEAL_REASON).isPresent());
        assertEquals(RESPONDENT_REVIEW.toString(), returnedCallbackResponse.getState().toString());
    }


    @Test
    public void it_cannot_handle_callback_if_feature_not_enabled() {

        when(callback.getEvent()).thenReturn(REINSTATE_APPEAL);
        when(featureToggler.getValue("reinstate-feature", false)).thenReturn(false);

        assertThatThrownBy(() -> reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_error_for_state_before_end_appeal_unknown() {

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
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reinstateAppealStateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback_for_all_events() {

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
    public void should_not_allow_null_arguments() {

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
