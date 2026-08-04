package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@NoArgsConstructor
public class CaseMessage {

    private String id;
    private String subject;
    private String name;
    private String body;
    private List<IdValue<Document>>  attachments;
    private YesOrNo isHearingRelated;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime createdOn;
    private String createdBy;
    private String parentId;
    private String messageType;
    private YesOrNo isClosed;

}
