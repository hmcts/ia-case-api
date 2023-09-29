package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_TRANSFERRED_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendNotificationHandlerTest {

    @Mock
    private NotificationSender<AsylumCase> notificationSender;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private SendNotificationHandler sendNotificationHandler;

    @BeforeEach
    public void setUp() {

        sendNotificationHandler =
            new SendNotificationHandler(notificationSender, featureToggler);

        ReflectionTestUtils.setField(sendNotificationHandler, "isSaveAndContinueEnabled", true);

        lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);
        lenient().when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        lenient().when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
    }

    @Test
    void should_send_notification_and_update_the_case() {

        Arrays.asList(
            Event.SUBMIT_APPEAL,
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
            Event.DECISION_WITHOUT_HEARING,
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
            Event.MARK_APPEAL_PAID,
            Event.MAKE_AN_APPLICATION,
            Event.REINSTATE_APPEAL,
            Event.DECIDE_AN_APPLICATION,
            Event.REQUEST_NEW_HEARING_REQUIREMENTS,
            Event.RECORD_REMISSION_DECISION,
            Event.REQUEST_FEE_REMISSION,
            Event.MANAGE_FEE_UPDATE,
            Event.REQUEST_FEE_REMISSION,
            Event.RECORD_OUT_OF_TIME_DECISION,
            Event.UPDATE_PAYMENT_STATUS,
            Event.ADA_SUITABILITY_REVIEW,
            Event.TRANSFER_OUT_OF_ADA,
            Event.MARK_APPEAL_AS_ADA,
            Event.REMOVE_DETAINED_STATUS,
            Event.MARK_APPEAL_AS_DETAINED,
            Event.CREATE_CASE_LINK,
            Event.MAINTAIN_CASE_LINKS,
            Event.MARK_AS_READY_FOR_UT_TRANSFER,
            Event.REQUEST_RESPONSE_AMEND,
            Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
            when(featureToggler.getValue("aip-ftpa-feature", false)).thenReturn(true);
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
    void should_notify_case_officer_that_case_is_listed() {

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
    void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, sendNotificationHandler.getDispatchPriority());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> sendNotificationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
            when(featureToggler.getValue("aip-ftpa-feature", false)).thenReturn(true);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendNotificationHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        Event.SUBMIT_APPEAL,
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
                        Event.DECISION_WITHOUT_HEARING,
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
                        Event.MARK_APPEAL_PAID,
                        Event.MAKE_AN_APPLICATION,
                        Event.REINSTATE_APPEAL,
                        Event.DECIDE_AN_APPLICATION,
                        Event.REQUEST_NEW_HEARING_REQUIREMENTS,
                        Event.RECORD_REMISSION_DECISION,
                        Event.REQUEST_FEE_REMISSION,
                        Event.MANAGE_FEE_UPDATE,
                        Event.RECORD_OUT_OF_TIME_DECISION,
                        Event.END_APPEAL_AUTOMATICALLY,
                        Event.UPDATE_PAYMENT_STATUS,
                        Event.ADA_SUITABILITY_REVIEW,
                        Event.TRANSFER_OUT_OF_ADA,
                        Event.MARK_APPEAL_AS_ADA,
                        Event.REMOVE_DETAINED_STATUS,
                        Event.MARK_APPEAL_AS_DETAINED,
                        Event.CREATE_CASE_LINK,
                        Event.MAINTAIN_CASE_LINKS,
                        Event.MARK_AS_READY_FOR_UT_TRANSFER,
                        Event.UPDATE_DETENTION_LOCATION,
                        Event.REQUEST_RESPONSE_AMEND,
                        Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
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
    void it_can_not_handle_callback_if_ex_ada_with_submitted_hearing_req() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            boolean canHandle = sendNotificationHandler.canHandle(callbackStage, callback);

            if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW) {

                assertFalse(canHandle);
            }
        }

        reset(callback);
    }

    @Test
    void can_handle_payment_appeal_when_paid_aip_appeal() {
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        assertTrue(sendNotificationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void cannot_handle_payment_appeal_when_unpaid_aip_appeal() {
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        assertFalse(sendNotificationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void cannot_handle_payment_appeal_when_non_aip_appeal() {
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(sendNotificationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

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
