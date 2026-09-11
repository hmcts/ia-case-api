package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryInfo;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SupplementaryDetailsResponse {

    private List<SupplementaryInfo> supplementaryInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MissingSupplementaryInfo missingSupplementaryInfo;

}
