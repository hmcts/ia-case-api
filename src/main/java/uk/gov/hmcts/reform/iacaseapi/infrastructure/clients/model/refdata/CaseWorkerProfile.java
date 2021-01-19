package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseWorkerProfile {

    String caseWorkerId;
    String firstName;
    String lastName;
    String emailId;
    Long userTypeId;
    String region;
    Integer regionId;
    Boolean suspended;
    LocalDateTime createdDate;
    LocalDateTime lastUpdate;

}
