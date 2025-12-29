package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Callback<T extends CaseData> {

    @Getter
    @JsonProperty("event_id")
    private Event event;

    private CaseDetails<T> caseDetails;
    @Getter
    private Optional<CaseDetails<T>> caseDetailsBefore = Optional.empty();

    @Setter
    @Getter
    private String pageId = "";

    private Callback() {
        // noop -- for deserializer
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

    public CaseDetails<T> getCaseDetails() {

        if (caseDetails == null) {
            throw new RequiredFieldMissingException("caseDetails field is required");
        }

        return caseDetails;
    }
}
