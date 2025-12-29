package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryValues {

    @JsonProperty("category_key")
    private String categoryKey;

    @JsonProperty("hint_text_cy")
    private String hintTextCy;

    @JsonProperty("hint_text_en")
    private String hintTextEn;

    @JsonProperty("key")
    private String key;

    @JsonProperty("lov_order")
    private int lovOrder;

    @JsonProperty("parent_category")
    private String parentCategory;

    @JsonProperty("parent_key")
    private String parentKey;

    @JsonProperty("value_cy")
    private String valueCy;

    @JsonProperty("value_en")
    private String valueEn;

    @JsonProperty("child_nodes")
    private List<CategoryValues> childNodes;

    @JsonProperty("active_flag")
    private String activeFlag;
}
