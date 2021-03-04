package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@Getter
@EqualsAndHashCode
@ToString
public class OutOfTimeDecisionDetails {

    private String decisionType;
    private String decisionMaker;
    private Document decisionDocument;

    private OutOfTimeDecisionDetails() {

    }

    public OutOfTimeDecisionDetails(String decisionType, String decisionMaker, Document decisionDocument) {
        this.decisionType = decisionType;
        this.decisionMaker = decisionMaker;
        this.decisionDocument = decisionDocument;
    }

}
