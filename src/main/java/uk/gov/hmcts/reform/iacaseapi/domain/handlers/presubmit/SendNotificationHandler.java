package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Component
public class SendNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationSender<AsylumCase> notificationSender;

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
                   Event.LIST_CASE,
                   Event.EDIT_CASE_LISTING,
                   Event.END_APPEAL,
                   Event.UPLOAD_HOME_OFFICE_BUNDLE,
                   Event.REQUEST_CASE_BUILDING,
                   Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
                   Event.REQUEST_RESPONSE_REVIEW,
                   Event.SUBMIT_CASE,
                   Event.SEND_DECISION_AND_REASONS,
                   Event.GENERATE_HEARING_BUNDLE,
                   Event.UPLOAD_ADDITIONAL_EVIDENCE,
                   Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
                   Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP,
                   Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE
               ).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }
}
