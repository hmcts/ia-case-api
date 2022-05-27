package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class IdValue<T> {

    private String id = "";
    private T value;

    private IdValue() {
        // noop -- for deserializer
    }

    public IdValue(
        String id,
        T value
    ) {
        requireNonNull(id);
        requireNonNull(value);

        this.id = id;
        this.value = value;
    }

    public String getId() {
        requireNonNull(id);
        return id;
    }

    public T getValue() {
        requireNonNull(value);
        return value;
    }
}
