package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import java.util.LinkedHashSet;
import java.util.Set;

public class CcdEventPreSubmitResponse<T extends CaseData> {

    private final T data;
    private final Set<String> errors = new LinkedHashSet<>();
    private final Set<String> warnings = new LinkedHashSet<>();

    public CcdEventPreSubmitResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public Set<String> getWarnings() {
        return warnings;
    }
}
