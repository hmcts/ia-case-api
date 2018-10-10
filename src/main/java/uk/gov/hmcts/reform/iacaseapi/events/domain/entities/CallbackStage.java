package uk.gov.hmcts.reform.iacaseapi.events.domain.entities;

public enum CallbackStage {

    ABOUT_TO_START("aboutToStart"),
    ABOUT_TO_SUBMIT("aboutToSubmit"),
    SUBMITTED("submitted");

    private final String id;

    CallbackStage(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
