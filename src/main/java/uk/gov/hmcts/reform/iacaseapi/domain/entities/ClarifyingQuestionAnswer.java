package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class ClarifyingQuestionAnswer {
    private String dateSent;
    private String dueDate;
    private String dateResponded;
    private String question;
    private String answer;
    private String directionId;
    private List<IdValue<Document>> supportingEvidence;

    private ClarifyingQuestionAnswer() {
    }

    public ClarifyingQuestionAnswer(String dateSent, String dueDate, String dateResponded, String question, String answer, String directionId, List<IdValue<Document>> supportingEvidence) {
        this.dateSent = dateSent;
        this.dueDate = dueDate;
        this.dateResponded = dateResponded;
        this.question = question;
        this.answer = answer;
        this.directionId = directionId;
        this.supportingEvidence = supportingEvidence;
    }

    public String getDateSent() {
        return dateSent;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDateResponded() {
        return dateResponded;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getDirectionId() {
        return directionId;
    }

    public List<IdValue<Document>> getSupportingEvidence() {
        return supportingEvidence;
    }
}
