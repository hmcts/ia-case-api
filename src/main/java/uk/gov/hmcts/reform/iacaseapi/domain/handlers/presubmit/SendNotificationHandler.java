package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Component
public class SendNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final int REMOVE_APPEAL_FROM_ONLINE_REASON_MAX_LENGTH = 918;
    private final NotificationSender<AsylumCase> notificationSender;
    @Value("${featureFlag.isSaveAndContinueEnabled}")
    private boolean isSaveAndContinueEnabled;

    public SendNotificationHandler(
        NotificationSender<AsylumCase> notificationSender
    ) {
        this.notificationSender = notificationSender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && getEventsToHandle().contains(callback.getEvent());
    }

    private List<Event> getEventsToHandle() {
        List<Event> eventsToHandle = Lists.newArrayList(
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
            Event.LIST_CASE,
            Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
            Event.EDIT_CASE_LISTING,
            Event.END_APPEAL,
            Event.UPLOAD_HOME_OFFICE_BUNDLE,
            Event.REQUEST_CASE_BUILDING,
            Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
            Event.REQUEST_RESPONSE_REVIEW,
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
            Event.SUBMIT_CASE,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.LINK_APPEAL,
            Event.UNLINK_APPEAL,
            Event.EDIT_DOCUMENTS,
            Event.LIST_CMA,
            Event.FORCE_REQUEST_CASE_BUILDING,
            Event.LEADERSHIP_JUDGE_FTPA_DECISION,
            Event.REQUEST_RESPONSE_AMEND,
            Event.RESIDENT_JUDGE_FTPA_DECISION,
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
            Event.END_APPEAL_AUTOMATICALLY,
            Event.UPDATE_PAYMENT_STATUS
        );
        if (!isSaveAndContinueEnabled) {
            eventsToHandle.add(Event.BUILD_CASE);
        }
        return eventsToHandle;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        if (Event.REMOVE_APPEAL_FROM_ONLINE == callback.getEvent() && isAipJourney(asylumCase)) {
            boolean isValid = validateReasonForTakingAppealOffline(asylumCase);

            if (!isValid) {
                PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                String errorMessage = "Reasons must not be more than " + REMOVE_APPEAL_FROM_ONLINE_REASON_MAX_LENGTH + " characters long";
                response.addError(errorMessage);

                return response;
            }
        }

        AsylumCase asylumCaseWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }

    private boolean validateReasonForTakingAppealOffline(AsylumCase asylumCase) {
        Optional<List<IdValue<Subscriber>>> optionalSubscribers = asylumCase.read(SUBSCRIPTIONS);
        String removeAppealFromOnlineReason = asylumCase.read(REMOVE_APPEAL_FROM_ONLINE_REASON, String.class)
            .orElse("").trim();
        boolean wantsSms = optionalSubscribers.orElse(Collections.emptyList()).stream()
            .filter(subscriber -> YES.equals(subscriber.getValue().getWantsSms())).count() > 0;

        return wantsSms
            ? wantsSms && removeAppealFromOnlineReason.length() <= REMOVE_APPEAL_FROM_ONLINE_REASON_MAX_LENGTH
            : true;
    }

    private boolean isAipJourney(AsylumCase asylumCase) {
        return asylumCase
            .read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == JourneyType.AIP).orElse(false);
    }
}
