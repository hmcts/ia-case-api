package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@JsonDeserialize(builder = AuditDetails.AuditDetailsBuilder.class)
@Builder(builderClassName = "AuditDetailsBuilder", toBuilder = true)
public class AuditDetails {
    String idamUserId;
    String user;
    List<String> documentIds;
    List<String> documentNames;
    long caseId;
    String reason;
    LocalDateTime dateTime;


    @JsonPOJOBuilder(withPrefix = "")
    public static class AuditDetailsBuilder {
    }

}
