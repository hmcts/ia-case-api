package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Jacksonized
@AllArgsConstructor
public class CaseWorkerProfile {

    String id;
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("last_name")
    String lastName;

}
