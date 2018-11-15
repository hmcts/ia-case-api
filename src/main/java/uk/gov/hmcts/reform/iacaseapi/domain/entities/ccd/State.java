package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum State {

    APPEAL_STARTED("appealStarted");

    private final String id;

    State(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
