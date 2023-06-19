package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;

@Value
@Builder
@Jacksonized
public class HearingRecordingDocument implements HasDocument {
    Document document;
    String description;
}
