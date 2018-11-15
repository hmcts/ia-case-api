package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

public enum PreSubmitCallbackStage {

    ABOUT_TO_START("aboutToStart"),
    ABOUT_TO_SUBMIT("aboutToSubmit");

    private final String id;

    PreSubmitCallbackStage(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
