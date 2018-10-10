package uk.gov.hmcts.reform.iacaseapi.forms.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EventWithCaseData<T extends CaseData> {

    private Event event;
    private String eventToken;

    @JsonProperty("data")
    private T caseData;

    private EventWithCaseData() {
        // noop -- for deserializer
    }

    public EventWithCaseData(
        Event event,
        String eventToken,
        T caseData
    ) {
        this.event = event;
        this.eventToken = eventToken;
        this.caseData = caseData;
    }

    public Event getEvent() {
        return event;
    }

    public String getEventToken() {
        return eventToken;
    }

    public T getCaseData() {
        return caseData;
    }
}
