package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CcdEvent<T extends CaseData> {

    private CaseDetails<T> caseDetails;
    private CaseDetails<T> caseDetailsBefore;
    private EventId eventId;

    private CcdEvent() {
        // noop -- for deserializer
    }

    public CcdEvent(
        CaseDetails<T> caseDetails,
        CaseDetails<T> caseDetailsBefore,
        EventId eventId
    ) {
        this.caseDetails = caseDetails;
        this.caseDetailsBefore = caseDetailsBefore;
        this.eventId = eventId;
    }

    public CaseDetails<T> getCaseDetails() {
        return caseDetails;
    }

    public CaseDetails<T> getCaseDetailsBefore() {
        return caseDetailsBefore;
    }

    public EventId getEventId() {
        return eventId;
    }
}
