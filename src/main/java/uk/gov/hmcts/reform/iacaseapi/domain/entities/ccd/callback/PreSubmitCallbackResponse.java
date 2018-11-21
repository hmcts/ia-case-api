package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

public class PreSubmitCallbackResponse<T extends CaseData> {

    private final T data;
    private final Set<String> errors = new LinkedHashSet<>();

    public PreSubmitCallbackResponse(
        T data
    ) {
        requireNonNull(data);
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    public Set<String> getErrors() {
        return ImmutableSet.copyOf(errors);
    }
}
