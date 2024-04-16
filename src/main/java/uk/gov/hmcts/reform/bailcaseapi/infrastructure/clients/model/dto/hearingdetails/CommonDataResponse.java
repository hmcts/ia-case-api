package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails;

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
public class CommonDataResponse {

    @JsonProperty("list_of_values")
    private List<CategoryValues> categoryValues;

}
