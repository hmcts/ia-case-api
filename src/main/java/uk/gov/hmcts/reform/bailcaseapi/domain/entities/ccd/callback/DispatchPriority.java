package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback;

public enum DispatchPriority {

    EARLIEST("earliest"),
    EARLY("early"),
    LATE("late"),
    LATEST("latest");

    private final String id;

    DispatchPriority(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
