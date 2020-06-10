package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

public class HomeOfficeMetadata {

    private String code;
    private String valueBoolean;
    private String valueDateTime;
    private String valueString;

    private HomeOfficeMetadata() {
    }

    public HomeOfficeMetadata(String code, String valueBoolean, String valueDateTime, String valueString) {
        this.code = code;
        this.valueBoolean = valueBoolean;
        this.valueDateTime = valueDateTime;
        this.valueString = valueString;
    }

    public String getCode() {
        return code;
    }

    public String getValueBoolean() {
        return valueBoolean;
    }

    public String getValueDateTime() {
        return valueDateTime;
    }

    public String getValueString() {
        return valueString;
    }
}
