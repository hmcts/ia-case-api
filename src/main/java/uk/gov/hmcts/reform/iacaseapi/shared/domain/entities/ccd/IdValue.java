package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd;

public class IdValue<T> {

    private String id = "";
    private T value = null;

    private IdValue() {
        // noop -- for deserializer
    }

    public IdValue(
        String id,
        T value
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }

        this.id = id;
        this.value = value;
    }

    public String getId() {
        if (id == null) {
            throw new IllegalStateException("id cannot be null");
        }

        return id;
    }

    public T getValue() {
        if (value == null) {
            throw new IllegalStateException("value cannot be null");
        }

        return value;
    }
}
