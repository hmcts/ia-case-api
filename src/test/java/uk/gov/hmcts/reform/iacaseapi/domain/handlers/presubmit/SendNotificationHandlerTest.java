package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SendNotificationHandlerTest {

    @Mock private NotificationSender<AsylumCase> notificationSender;
    @Mock private Callback<AsylumCase> callback;

    private SendNotificationHandler sendNotificationHandler;

    @Before
    public void setUp() {

        sendNotificationHandler =
            new SendNotificationHandler(notificationSender);

        ReflectionTestUtils.setField(sendNotificationHandler, "isSaveAndContinueEnabled", true);
    }

    @Test
    public void should_send_notification_and_update_the_case() {

        Arrays.asList(
            Event.SUBMIT_APPEAL,
            Event.PAY_AND_SUBMIT_APPEAL,
            Event.SEND_DIRECTION,
            Event.CHANGE_DIRECTION_DUE_DATE,
            Event.REQUEST_RESPONDENT_EVIDENCE,
            Event.UPLOAD_RESPONDENT_EVIDENCE,
            Event.REQUEST_RESPONDENT_REVIEW,
            Event.ADD_APPEAL_RESPONSE,
            Event.REQUEST_HEARING_REQUIREMENTS,
            Event.DRAFT_HEARING_REQUIREMENTS,
            Event.REVIEW_HEARING_REQUIREMENTS,
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            Event.LIST_CASE,
            Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
            Event.EDIT_CASE_LISTING,
            Event.END_APPEAL,
            Event.UPLOAD_HOME_OFFICE_BUNDLE,
            Event.REQUEST_CASE_BUILDING,
            Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
            Event.REQUEST_RESPONSE_REVIEW,
            Event.SUBMIT_CASE,
            Event.SEND_DECISION_AND_REASONS,
            Event.UPLOAD_ADDITIONAL_EVIDENCE,
            Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
            Event.UPLOAD_ADDENDUM_EVIDENCE,
            Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP,
            Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
            Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
            Event.REQUEST_REASONS_FOR_APPEAL,
            Event.SUBMIT_REASONS_FOR_APPEAL,
            Event.UPDATE_HEARING_ADJUSTMENTS,
            Event.REMOVE_APPEAL_FROM_ONLINE,
            Event.CHANGE_HEARING_CENTRE,
            Event.APPLY_FOR_FTPA_APPELLANT,
            Event.APPLY_FOR_FTPA_RESPONDENT,
            Event.REVIEW_TIME_EXTENSION,
            Event.SUBMIT_TIME_EXTENSION,
            Event.SEND_DIRECTION_WITH_QUESTIONS,
            Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS,
            Event.REQUEST_CASE_EDIT,
            Event.FORCE_CASE_TO_CASE_UNDER_REVIEW,
            Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS,
            Event.SUBMIT_TIME_EXTENSION,
            Event.ADJOURN_HEARING_WITHOUT_DATE,
            Event.RESTORE_STATE_FROM_ADJOURN,
            Event.REQUEST_CMA_REQUIREMENTS,
            Event.SUBMIT_CMA_REQUIREMENTS,
            Event.LIST_CMA,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.UNLINK_APPEAL,
            Event.LINK_APPEAL,
            Event.LEADERSHIP_JUDGE_FTPA_DECISION,
            Event.RESIDENT_JUDGE_FTPA_DECISION,
            Event.REQUEST_RESPONSE_AMEND,
            Event.PAYMENT_APPEAL,
            Event.MARK_APPEAL_PAID,
            Event.MAKE_AN_APPLICATION,
            Event.REINSTATE_APPEAL,
            Event.DECIDE_AN_APPLICATION,
            Event.REQUEST_NEW_HEARING_REQUIREMENTS
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(notificationSender.send(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(notificationSender, times(1)).send(callback);

            reset(callback);
            reset(notificationSender);
        });
    }

    @Test
    public void should_notify_case_officer_that_case_is_listed() {

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

        when(notificationSender.send(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());

        verify(notificationSender, times(1)).send(callback);

        reset(callback);
        reset(notificationSender);
    }

    @Test
    public void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, sendNotificationHandler.getDispatchPriority());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendNotificationHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        Event.SUBMIT_APPEAL,
                        Event.PAY_AND_SUBMIT_APPEAL,
                        Event.SEND_DIRECTION,
                        Event.CHANGE_DIRECTION_DUE_DATE,
                        Event.REQUEST_RESPONDENT_EVIDENCE,
                        Event.UPLOAD_RESPONDENT_EVIDENCE,
                        Event.REQUEST_RESPONDENT_REVIEW,
                        Event.ADD_APPEAL_RESPONSE,
                        Event.REQUEST_HEARING_REQUIREMENTS,
                        Event.DRAFT_HEARING_REQUIREMENTS,
                        Event.REVIEW_HEARING_REQUIREMENTS,
                        Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
                        Event.LIST_CASE,
                        Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
                        Event.EDIT_CASE_LISTING,
                        Event.END_APPEAL,
                        Event.UPLOAD_HOME_OFFICE_BUNDLE,
                        Event.REQUEST_CASE_BUILDING,
                        Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
                        Event.REQUEST_RESPONSE_REVIEW,
                        Event.SUBMIT_CASE,
                        Event.SEND_DECISION_AND_REASONS,
                        Event.UPLOAD_ADDITIONAL_EVIDENCE,
                        Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
                        Event.UPLOAD_ADDENDUM_EVIDENCE,
                        Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP,
                        Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
                        Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
                        Event.REQUEST_REASONS_FOR_APPEAL,
                        Event.SUBMIT_REASONS_FOR_APPEAL,
                        Event.UPDATE_HEARING_ADJUSTMENTS,
                        Event.REMOVE_APPEAL_FROM_ONLINE,
                        Event.CHANGE_HEARING_CENTRE,
                        Event.APPLY_FOR_FTPA_APPELLANT,
                        Event.APPLY_FOR_FTPA_RESPONDENT,
                        Event.REVIEW_TIME_EXTENSION,
                        Event.SUBMIT_TIME_EXTENSION,
                        Event.SEND_DIRECTION_WITH_QUESTIONS,
                        Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS,
                        Event.REQUEST_CASE_EDIT,
                        Event.FORCE_CASE_TO_CASE_UNDER_REVIEW,
                        Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS,
                        Event.SUBMIT_TIME_EXTENSION,
                        Event.ADJOURN_HEARING_WITHOUT_DATE,
                        Event.RESTORE_STATE_FROM_ADJOURN,
                        Event.REQUEST_CMA_REQUIREMENTS,
                        Event.SUBMIT_CMA_REQUIREMENTS,
                        Event.LIST_CMA,
                        Event.EDIT_APPEAL_AFTER_SUBMIT,
                        Event.UNLINK_APPEAL,
                        Event.LINK_APPEAL,
                        Event.EDIT_DOCUMENTS,
                        Event.FORCE_REQUEST_CASE_BUILDING,
                        Event.LEADERSHIP_JUDGE_FTPA_DECISION,
                        Event.RESIDENT_JUDGE_FTPA_DECISION,
                        Event.REQUEST_RESPONSE_AMEND,
                        Event.PAYMENT_APPEAL,
                        Event.MARK_APPEAL_PAID,
                        Event.MAKE_AN_APPLICATION,
                        Event.REINSTATE_APPEAL,
                        Event.DECIDE_AN_APPLICATION,
                        Event.REQUEST_NEW_HEARING_REQUIREMENTS
                    ).contains(event)) {

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

        assertThatThrownBy(() -> sendNotificationHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendNotificationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendNotificationHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
