package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Callback<T extends CaseData> {

    @JsonProperty("event_id")
    private Event event;

    private CaseDetails<T> caseDetails;
    private Optional<CaseDetails<T>> caseDetailsBefore = Optional.empty();

    private String pageId = "";

    private Callback() {
    }

    public Callback(
        CaseDetails<T> caseDetails,
        Optional<CaseDetails<T>> caseDetailsBefore,
        Event event
    ) {
        requireNonNull(caseDetails);
        requireNonNull(caseDetailsBefore);
        requireNonNull(event);

        this.caseDetails = caseDetails;
        this.caseDetailsBefore = caseDetailsBefore;
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public CaseDetails<T> getCaseDetails() {
        if (caseDetails == null) {
            throw new RequiredFieldMissingException("caseDetails field is required");
        }
        return caseDetails;
    }

    public Optional<CaseDetails<T>> getCaseDetailsBefore() {
        return caseDetailsBefore;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
}
