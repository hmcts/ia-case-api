package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static java.util.Objects.requireNonNull;

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
        requireNonNull(code);
        return code;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

}
