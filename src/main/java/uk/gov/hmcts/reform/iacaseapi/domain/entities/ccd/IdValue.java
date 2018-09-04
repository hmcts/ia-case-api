package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public class IdValue<T> {

    private String id = "";
    private T value = null;

    private IdValue() {
        // noop -- for deserializer
    }

    public IdValue(String id, T value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public T getValue() {
        if (value == null) {
            throw new IllegalStateException("Value cannot be null");
        }

        return value;
    }
}
