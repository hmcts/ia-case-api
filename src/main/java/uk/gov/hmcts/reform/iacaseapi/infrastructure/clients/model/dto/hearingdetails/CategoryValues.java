package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryValues {

    @JsonProperty("category_key")
    private String categoryKey;

    @JsonProperty("key")
    private String key;

    @JsonProperty("value_en")
    private String valueEn;

    @JsonProperty("value_cy")
    private String valueCy;

    @JsonProperty("child_nodes")
    private List<CategorySubValues> childNodes;

}
