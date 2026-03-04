package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseMessage {

    private String id;
    private String subject;
    private String name;
    private String body;
    private List<Document> attachments;
    private YesOrNo isHearingRelated;
    private LocalDate hearingDate;
    private OffsetDateTime createdOn;
    private String createdBy;
    private String parentId;

}
