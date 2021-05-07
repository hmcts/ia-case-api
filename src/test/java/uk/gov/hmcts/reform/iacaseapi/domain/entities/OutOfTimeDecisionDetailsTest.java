package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class OutOfTimeDecisionDetailsTest {

    private String decisionType = "Approved";
    private String decisionMaker = "Tribunal CaseWorker";
    private Document decisionDocument = mock(Document.class);

    @Test
    void should_hold_out_of_time_decision_details() {

        OutOfTimeDecisionDetails outOfTimeDecisionDetails =
            new OutOfTimeDecisionDetails(decisionType, decisionMaker, decisionDocument);

        assertEquals(decisionType, outOfTimeDecisionDetails.getDecisionType());
        assertEquals(decisionMaker, outOfTimeDecisionDetails.getDecisionMaker());
        assertEquals(decisionDocument, outOfTimeDecisionDetails.getDecisionDocument());
    }
}
