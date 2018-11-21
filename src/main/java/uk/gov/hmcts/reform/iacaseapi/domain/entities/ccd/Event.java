package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum Event {

    START_APPEAL("startAppeal");

    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
