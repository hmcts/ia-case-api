package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

public enum DispatchPriority {

    EARLY("early"),
    LATE("late");

    private final String id;

    DispatchPriority(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
