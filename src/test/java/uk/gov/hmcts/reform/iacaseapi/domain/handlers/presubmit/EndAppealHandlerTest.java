package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class EndAppealHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private CaseDetails<AsylumCase> previousCaseDetails;

    @Captor private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "30/01/2019";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "31/01/2019";
    private String applicationStatus = "In progress";
    private State previousState = State.AWAITING_RESPONDENT_EVIDENCE;

    private EndAppealHandler endAppealHandler;

    private LocalDate date = LocalDate.now();

    @Before
    public void setup() {

        when(dateProvider.now()).thenReturn(date);
        endAppealHandler = new EndAppealHandler(dateProvider);
        when(previousCaseDetails.getState()).thenReturn(previousState);
        when(callback
            .getCaseDetailsBefore()).thenReturn(Optional.of(previousCaseDetails));

    }

    @Test
    public void should_be_set_on_early() {

        assertEquals(DispatchPriority.EARLY, endAppealHandler.getDispatchPriority());
    }

    @Test
    public void should_set_end_appeal_date_as_now_and_visibility_flags() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(END_APPEAL_DATE, date.toString());
        verify(asylumCase).write(RECORD_APPLICATION_ACTION_DISABLED, YesOrNo.YES);
    }


    @Test
    public void should_set_state_before_end_appeal() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(STATE_BEFORE_END_APPEAL, previousState);
        verify(asylumCase).clear(REINSTATE_APPEAL_REASON);
        verify(asylumCase).clear(REINSTATED_DECISION_MAKER);
        verify(asylumCase).clear(APPEAL_STATUS);
        verify(asylumCase).clear(REINSTATE_APPEAL_DATE);
    }

    @Test
    public void should_throw_exception_if_previous_state_not_found() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback
            .getCaseDetailsBefore()).thenReturn(Optional.empty());
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);

        assertThatThrownBy(() -> endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("cannot find previous case state")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = endAppealHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event == Event.END_APPEAL) {

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

        assertThatThrownBy(() -> endAppealHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_handle_withdraw_and_set_application_status_to_completed() {

        List<IdValue<Application>> expectedApplications = newArrayList(new IdValue<>("1", new Application(
            Collections.emptyList(),
            applicationSupplier,
            ApplicationType.WITHDRAW.toString(),
            applicationReason,
            applicationDate,
            applicationDecision,
            applicationDecisionReason,
            applicationDateOfDecision,
            applicationStatus
        )));

        when(callback.getEvent()).thenReturn(END_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(expectedApplications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).clear(APPLICATION_WITHDRAW_EXISTS);
        verify(asylumCase).clear(DISABLE_OVERVIEW_PAGE);
        verify(asylumCase).clear(REINSTATE_APPEAL_REASON);
        verify(asylumCase).clear(REINSTATED_DECISION_MAKER);
        verify(asylumCase).clear(APPEAL_STATUS);
        verify(asylumCase).clear(REINSTATE_APPEAL_DATE);
        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }
}
