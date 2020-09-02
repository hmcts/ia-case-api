package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

public class CodeWithDescription {

    private String code;
    private String description;

    public CodeWithDescription(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private CodeWithDescription() {

    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
