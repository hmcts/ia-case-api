package uk.gov.hmcts.reform.iacaseapi.forms.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EventWithCaseData<T extends CaseData> {

    private final Event event;
    private final String eventToken;

    @JsonProperty("data")
    private Optional<T> caseData;

    public EventWithCaseData(
        Event event,
        String eventToken
    ) {
        this(
            event,
            eventToken,
            null
        );
    }

    public EventWithCaseData(
        Event event,
        String eventToken,
        T caseData
    ) {
        this.event = event;
        this.eventToken = eventToken;
        this.caseData = Optional.ofNullable(caseData);
    }

    public Event getEvent() {
        return event;
    }

    public String getEventToken() {
        return eventToken;
    }

    public Optional<T> getCaseData() {
        return caseData;
    }

    public void setCaseData(
        T caseData
    ) {
        this.caseData = Optional.of(caseData);
    }
}
