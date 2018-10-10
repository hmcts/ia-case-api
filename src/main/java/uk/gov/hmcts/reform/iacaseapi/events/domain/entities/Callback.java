package uk.gov.hmcts.reform.iacaseapi.events.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Callback<T extends CaseData> {

    private CaseDetails<T> caseDetails;
    private CaseDetails<T> caseDetailsBefore;
    private EventId eventId;

    private Callback() {
        // noop -- for deserializer
    }

    public Callback(
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
