package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum State {

    APPEAL_STARTED("appealStarted"),
    APPEAL_SUBMITTED("appealSubmitted");

    private final String id;

    State(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
