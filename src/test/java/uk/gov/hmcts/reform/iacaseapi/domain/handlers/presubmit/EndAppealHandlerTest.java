package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;

import java.time.LocalDate;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EndAppealHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private CaseDetails<AsylumCase> previousCaseDetails;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

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

    @BeforeEach
    public void setup() {

        when(dateProvider.now()).thenReturn(date);
        endAppealHandler = new EndAppealHandler(dateProvider, iaHearingsApiService);
        when(previousCaseDetails.getState()).thenReturn(previousState);
        when(callback
            .getCaseDetailsBefore()).thenReturn(Optional.of(previousCaseDetails));
        when(iaHearingsApiService.aboutToSubmit(any())).thenReturn(asylumCase);

    }

    @Test
    void should_be_set_on_early() {

        assertEquals(DispatchPriority.EARLY, endAppealHandler.getDispatchPriority());
    }

    @Test
    void should_set_end_appeal_date_as_now_and_visibility_flags() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(END_APPEAL_DATE, date.toString());
        verify(asylumCase).write(RECORD_APPLICATION_ACTION_DISABLED, YesOrNo.YES);
        verify(iaHearingsApiService).aboutToSubmit(callback);
    }


    @Test
    void should_set_state_before_end_appeal() {

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
        verify(asylumCase).clear(MANUAL_CANCEL_HEARINGS_REQUIRED);
    }

    @Test
    void should_throw_exception_if_previous_state_not_found() {

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
    void should_throw_exception_if_previous_case_state_is_ended() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL_AUTOMATICALLY);
        when(caseDetails.getState()).thenReturn(State.ENDED);
        when(previousCaseDetails.getState()).thenReturn(State.ENDED);

        assertThatThrownBy(() -> endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Appeal has already been ended!")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_handle_paid_appeals() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(callback.getEvent()).thenReturn(Event.END_APPEAL_AUTOMATICALLY);

        assertThatThrownBy(() -> endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot auto end appeal as the payment is already made!")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_handle_pending_payment_appeals() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL_AUTOMATICALLY);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(iaHearingsApiService, never()).aboutToSubmit(callback);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = endAppealHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && (event == Event.END_APPEAL || event == Event.END_APPEAL_AUTOMATICALLY)) {

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
    void should_handle_withdraw_and_set_application_status_to_completed() {

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
        verify(asylumCase).clear(MANUAL_CANCEL_HEARINGS_REQUIRED);
        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"END_APPEAL", "END_APPEAL_AUTOMATICALLY"})
    void should_clear_appeal_ready_for_ut_transfer_field(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase).clear(APPEAL_READY_FOR_UT_TRANSFER);
        verify(asylumCase).clear(UT_APPEAL_REFERENCE_NUMBER);
    }

    @Test
    void should_successfully_delete_hearings() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(END_APPEAL);
        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(iaHearingsApiService).aboutToSubmit(callback);
        verify(asylumCase, never()).write(eq(MANUAL_CANCEL_HEARINGS_REQUIRED), any());
        verify(asylumCase).clear(LIST_CASE_HEARING_DATE);
    }

    @Test
    void should_fail_to_delete_hearings() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(END_APPEAL);
        when(asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(iaHearingsApiService).aboutToSubmit(callback);
        verify(asylumCase).clear(LIST_CASE_HEARING_DATE);
        verify(asylumCase, times(1))
            .write(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.YES);
    }

    @Test
    void should_fail_to_delete_hearings_when_hearings_service_is_down() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(END_APPEAL);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(iaHearingsApiService.aboutToSubmit(callback))
            .thenThrow(new AsylumCaseServiceResponseException("Error message", null));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(iaHearingsApiService).aboutToSubmit(callback);
        verify(asylumCase, times(1))
            .write(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.YES);
    }
}
