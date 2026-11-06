package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryInfo;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplementaryDetailsResponse {

    @JsonProperty("supplementary_info")
    private List<SupplementaryInfo> supplementaryInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("missing_supplementary_info")
    private MissingSupplementaryInfo missingSupplementaryInfo;

}
