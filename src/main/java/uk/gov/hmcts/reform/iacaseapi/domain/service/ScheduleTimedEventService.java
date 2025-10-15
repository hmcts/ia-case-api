package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class ScheduleTimedEventService {

    private final DateProvider dateProvider;
    private final Scheduler scheduler;

    public ScheduleTimedEventService(DateProvider dateProvider, Scheduler scheduler) {
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
    }

    public void scheduleTimedEvent(String caseId, ZonedDateTime scheduledDate, Event event) {
        try {
            scheduler.schedule(
                    new TimedEvent(
                            "",
                            event,
                            scheduledDate,
                            "IA",
                            "Asylum",
                            Long.parseLong(caseId)
                    )
            );
            log.info("Scheduled event " + event + " for case ID " + caseId);
        } catch (AsylumCaseServiceResponseException e) {
            log.info(e.getMessage());
        }
    }

    public void scheduleTimedEventNow(String caseId, Event event) {
        ZonedDateTime now = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault());
        scheduleTimedEvent(caseId, now, event);
    }
}
