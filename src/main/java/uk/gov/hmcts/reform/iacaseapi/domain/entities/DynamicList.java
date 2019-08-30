package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;

public class DynamicList {

    private Value value;
    private List<Value> list_items;

    public DynamicList(String value) {
        this.value = new Value(value, value);
    }

    private DynamicList() {
    }

    public List<Value> getList_items() {
        return list_items;
    }

    public DynamicList(Value value, List<Value> list_items) {
        this.value = value;
        this.list_items = list_items;
    }

    public Value getValue() {
        return value;
    }

}
