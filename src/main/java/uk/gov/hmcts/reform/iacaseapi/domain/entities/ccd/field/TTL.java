package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@Data
public class TTL {

    private String systemTTL;
    private String overrideTTL;
    private YesOrNo suspended;
}
