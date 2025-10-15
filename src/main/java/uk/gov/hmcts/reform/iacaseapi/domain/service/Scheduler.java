package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.time.ZonedDateTime;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

public interface Scheduler {

    TimedEvent schedule(TimedEvent timedEvent);

    boolean deleteSchedule(String timedEventId);

    void scheduleTimedEvent(String caseId, ZonedDateTime scheduledDate, Event event);

    void scheduleTimedEventNow(String caseId, Event event);
}
