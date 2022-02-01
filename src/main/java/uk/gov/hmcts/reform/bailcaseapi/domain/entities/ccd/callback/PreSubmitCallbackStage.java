package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback;

public enum PreSubmitCallbackStage {

    ABOUT_TO_START("aboutToStart"),
    ABOUT_TO_SUBMIT("aboutToSubmit"),
    MID_EVENT("midEvent");

    private final String id;

    PreSubmitCallbackStage(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
