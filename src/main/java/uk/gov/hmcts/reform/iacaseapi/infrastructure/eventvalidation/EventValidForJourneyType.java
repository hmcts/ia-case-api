package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

public class EventValidForJourneyType {
    private final String invalidReason;

    public EventValidForJourneyType() {
        this(null);
    }

    public EventValidForJourneyType(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public boolean isValid() {
        return invalidReason == null;
    }

    public String getInvalidReason() {
        return invalidReason;
    }
}
