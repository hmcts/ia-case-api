package uk.gov.hmcts.reform.iacaseapi.events.domain.entities;

import java.util.LinkedHashSet;
import java.util.Set;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;

public class PreSubmitCallbackResponse<T extends CaseData> {

    private final T data;
    private final Set<String> errors = new LinkedHashSet<>();
    private final Set<String> warnings = new LinkedHashSet<>();

    public PreSubmitCallbackResponse(T data) {
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
