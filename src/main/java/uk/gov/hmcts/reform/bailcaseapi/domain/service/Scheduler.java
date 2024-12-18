package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.TimedEvent;

public interface Scheduler {

    TimedEvent schedule(TimedEvent timedEvent);

    boolean deleteSchedule(String timedEventId);
}
