package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

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
