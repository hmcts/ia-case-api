package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DynamicList {

    private Value value;
    private List<Value> listItems;

    public DynamicList(String value) {
        this.value = new Value(value, value);
        this.listItems = new ArrayList<>();
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

}
