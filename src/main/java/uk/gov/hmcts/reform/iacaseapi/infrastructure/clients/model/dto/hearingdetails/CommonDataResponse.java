package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonDataResponse {

    @JsonProperty("list_of_values")
    private List<CategoryValues> categoryValues;



}
