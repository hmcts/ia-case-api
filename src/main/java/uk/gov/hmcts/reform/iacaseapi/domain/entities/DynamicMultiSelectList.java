package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DynamicMultiSelectList {

    private List<Value> value;
    private List<Value> listItems;

    public DynamicMultiSelectList(List<String> values) {
        this.value = values.stream().map(value -> new Value(value, value)).collect(Collectors.toList());
    }

    public DynamicMultiSelectList() {
    }

    public List<Value> getListItems() {
        return listItems;
    }

    public DynamicMultiSelectList(List<Value> values, List<Value> listItems) {
        this.value = values;
        this.listItems = listItems;
    }

    public List<Value> getValue() {
        return value;
    }

    public void setValue(List<Value> value) {
        this.value = value;
    }

}
