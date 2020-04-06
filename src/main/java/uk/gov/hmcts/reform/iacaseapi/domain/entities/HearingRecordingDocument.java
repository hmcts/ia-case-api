package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;

@Value
public class HearingRecordingDocument implements HasDocument {

    private Document document;
    private String description;

}
