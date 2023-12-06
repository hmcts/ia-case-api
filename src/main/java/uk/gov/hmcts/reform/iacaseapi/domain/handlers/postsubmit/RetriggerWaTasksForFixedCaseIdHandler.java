package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.readJsonFileList;

@Slf4j
@Component
public class RetriggerWaTasksForFixedCaseIdHandler implements PostSubmitCallbackHandler<AsylumCase> {

    private final boolean timedEventServiceEnabled;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    private List<String> caseIdList;
    private String filePath;


    public RetriggerWaTasksForFixedCaseIdHandler(
            @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
            @Value("${caseIdListJsonLocation}") String filePath,
            DateProvider dateProvider,
            Scheduler scheduler
    ) {
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.filePath = filePath;
    }

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        Event qualifyingEvent = Event.RE_TRIGGER_WA_BULK_TASKS;

        return  timedEventServiceEnabled
                && callback.getEvent() == qualifyingEvent;

    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        int scheduleDelayInMinutes = 5;
        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(scheduleDelayInMinutes);

        try {
            caseIdList = readJsonFileList(filePath, "caseIdList");
        } catch (IOException | NullPointerException e) {
            log.error("filePath is " + filePath);
            log.error(e.getMessage());
        }
        if (caseIdList != null && caseIdList.size() > 0) {
            for (int i = 0; i < caseIdList.size(); i++) {
                try {
                    scheduler.schedule(
                            new TimedEvent(
                                    "",
                                    Event.RE_TRIGGER_WA_TASKS,
                                    scheduledDate,
                                    "IA",
                                    "Asylum",
                                    Long.parseLong(caseIdList.get(i))
                            )
                    );
                } catch (AsylumCaseServiceResponseException e) {
                    log.error(e.getMessage());
                }
            }
        }

        return new PostSubmitCallbackResponse();
    }
}
