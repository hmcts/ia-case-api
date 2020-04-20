package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class ClarifyingQuestion {
    private String question;

    private ClarifyingQuestion() {
    }

    public ClarifyingQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
