package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Objects;

public class Value {

    private String code;
    private String label;

    private Value() {
        //no op constructor
    }

    public Value(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(code, value.code) &&
                Objects.equals(label, value.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, label);
    }

    @Override
    public String toString() {
        return "Value{" +
                "code='" + code + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
