package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

public enum YesOrNo {

    NO("No"),
    YES("Yes");

    private final String id;

    YesOrNo(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
