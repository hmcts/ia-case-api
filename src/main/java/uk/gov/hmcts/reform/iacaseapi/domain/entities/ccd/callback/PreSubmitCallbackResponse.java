package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;

public class PreSubmitCallbackResponse<T extends CaseData> {

    @Getter
    private T data;
    private final Set<String> errors = new LinkedHashSet<>();

    @Getter
    private State state;

    private PreSubmitCallbackResponse() {
        // noop -- for deserializer
    }

    public PreSubmitCallbackResponse(
        T data
    ) {
        requireNonNull(data);
        this.data = data;
    }

    public PreSubmitCallbackResponse(
        T data,
        State state
    ) {
        requireNonNull(data);
        this.data = data;
        this.state = state;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public PreSubmitCallbackResponse<T> withError(String error) {
        this.errors.add(error);
        return this;
    }

    public void addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    public Set<String> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

    public void setData(T data) {
        requireNonNull(data);
        this.data = data;
    }
}
