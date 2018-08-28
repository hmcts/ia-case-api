package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum Stage {

    ABOUT_TO_START("aboutToStart"),
    ABOUT_TO_SUBMIT("aboutToSubmit"),
    SUBMITTED("submitted");

    private final String id;

    Stage(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
