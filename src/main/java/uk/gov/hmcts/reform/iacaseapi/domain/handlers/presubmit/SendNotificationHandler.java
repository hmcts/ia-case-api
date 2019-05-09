package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Component
public class SendNotificationHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final NotificationSender<CaseDataMap> notificationSender;

    public SendNotificationHandler(
        NotificationSender<CaseDataMap> notificationSender
    ) {
        this.notificationSender = notificationSender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               &&
               Arrays.asList(
                   Event.SUBMIT_APPEAL,
                   Event.SEND_DIRECTION,
                   Event.REQUEST_RESPONDENT_EVIDENCE,
                   Event.UPLOAD_RESPONDENT_EVIDENCE,
                   Event.REQUEST_RESPONDENT_REVIEW,
                   Event.ADD_APPEAL_RESPONSE,
                   Event.REQUEST_HEARING_REQUIREMENTS
               ).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap CaseDataMapWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(CaseDataMapWithNotificationMarker);
    }
}
