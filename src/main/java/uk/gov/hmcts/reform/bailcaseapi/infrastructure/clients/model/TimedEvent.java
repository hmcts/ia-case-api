package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TimedEvent {

    private String id;
    private Event event;
    private ZonedDateTime scheduledDateTime;
    private String jurisdiction;
    private String caseType;
    private long caseId;

}
