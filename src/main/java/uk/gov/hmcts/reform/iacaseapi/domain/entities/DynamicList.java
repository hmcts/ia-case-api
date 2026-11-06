package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class DynamicList {

    private Value value;
    @JsonProperty("list_items")
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

    public void setValue(Value value) {
        this.value = value;
    }

}
