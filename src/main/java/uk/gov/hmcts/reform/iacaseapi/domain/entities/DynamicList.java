package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DynamicList {

    private Value value;
    private List<Value> listItems;

    public DynamicList(String value) {
        this.value = new Value(value, value);
    }

    private DynamicList() {
    }

    public List<Value> getListItems() {
        return listItems;
    }

    public DynamicList(Value value, List<Value> listItems) {
        this.value = value;
        this.listItems = listItems;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicList that = (DynamicList) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(listItems, that.listItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, listItems);
    }

    @Override
    public String toString() {
        return "DynamicList{" +
                "value=" + value +
                ", listItems=" + listItems +
                '}';
    }
}
