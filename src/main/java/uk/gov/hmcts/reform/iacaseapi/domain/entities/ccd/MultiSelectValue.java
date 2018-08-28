package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public class MultiSelectValue {

    private String id = "";
    private String value = "";

    private MultiSelectValue() {
        // noop -- for deserializer
    }

    public MultiSelectValue(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
