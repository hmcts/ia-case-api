package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousDecisionDetails {

    private String decisionDetailsDate;
    private String recordDecisionType;
    private Document uploadSignedDecisionNoticeDocument;
}
