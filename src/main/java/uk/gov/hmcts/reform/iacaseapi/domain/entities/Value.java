package uk.gov.hmcts.reform.iacaseapi.domain.entities;

public class Value {

    private String code;
    private String label;

    private Value() {

    }

    public Value(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
