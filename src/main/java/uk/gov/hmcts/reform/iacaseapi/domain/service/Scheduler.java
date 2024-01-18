package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

public interface Scheduler {

    TimedEvent schedule(TimedEvent timedEvent);

    boolean deleteSchedule(String timedEventId);
}
