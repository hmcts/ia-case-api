package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;

@Value
@RequiredArgsConstructor
public class HearingRecordingDocument implements HasDocument {
    Document document;
    String description;
}
