package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class AsylumCasePostNotificationApiSender implements PostNotificationSender<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String ccdSubmittedPath;
    private final boolean timedEventServiceEnabled;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;

    public AsylumCasePostNotificationApiSender(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.ccdSubmittedPath}") String ccdSubmittedPath,
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        DateProvider dateProvider,
        Scheduler scheduler
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
    }

    @Override
    public PostSubmitCallbackResponse send(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        if (timedEventServiceEnabled) {
            ZonedDateTime scheduledTime = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault())
                .plusSeconds(15);
            scheduler.schedule(
                new TimedEvent(
                    "",
                    Event.SAVE_NOTIFICATIONS_TO_DATA,
                    scheduledTime,
                    "IA",
                    "Asylum",
                    callback.getCaseDetails().getId()
                )
            );
        }

        return asylumCaseCallbackApiDelegator.delegatePostSubmit(
            callback,
            notificationsApiEndpoint + ccdSubmittedPath
        );
    }
}
