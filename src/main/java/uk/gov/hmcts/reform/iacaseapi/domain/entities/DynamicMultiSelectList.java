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

    private List<Value> values;
    private List<Value> listItems;

    public DynamicMultiSelectList(List<String> values) {
        this.values = values.stream().map(value -> new Value(value, value)).collect(Collectors.toList());
    }

    private DynamicMultiSelectList() {
    }

    public List<Value> getListItems() {
        return listItems;
    }

    public DynamicMultiSelectList(List<Value> values, List<Value> listItems) {
        this.values = values;
        this.listItems = listItems;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

}
