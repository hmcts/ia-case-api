package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CaseMessage {

    private String id;
    private String subject;
    private String name;
    private String body;
    private List<Document> attachments;
    @JsonProperty("isHearingRelated")
    private YesOrNo isHearingRelated;
    @JsonProperty("hearingDate")
    private LocalDate hearingDate;
    @JsonProperty("createdOn")
    private OffsetDateTime createdOn;
    @JsonProperty("createdBy")
    private String createdBy;
    @JsonProperty("parentId")
    private String parentId;

}
