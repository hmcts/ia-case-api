package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseWorkerProfile {

    String id;
    String firstName;
    String lastName;

}
