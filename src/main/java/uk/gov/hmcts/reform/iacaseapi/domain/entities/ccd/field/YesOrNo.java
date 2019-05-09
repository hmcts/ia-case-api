package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

public enum YesOrNo {

    No("No"),
    Yes("Yes");

    private final String id;

    YesOrNo(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
