package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PreviousDecisionDetailsTest {

    private final String decisionDetailsDate = "2023-02-21";
    private final String recordDecisionType = "conditionalBail";
    private final Document uploadSignedDecisionNoticeDocument = mock(Document.class);

    private final PreviousDecisionDetails previousDecisionDetails = new PreviousDecisionDetails(
        decisionDetailsDate,
        recordDecisionType,
        uploadSignedDecisionNoticeDocument
    );

    @Test
    void should_hold_onto_values() {
        assertEquals(decisionDetailsDate, previousDecisionDetails.getDecisionDetailsDate());
        assertEquals(recordDecisionType, previousDecisionDetails.getRecordDecisionType());
        assertEquals(uploadSignedDecisionNoticeDocument, previousDecisionDetails.getUploadSignedDecisionNoticeDocument());
    }
}
