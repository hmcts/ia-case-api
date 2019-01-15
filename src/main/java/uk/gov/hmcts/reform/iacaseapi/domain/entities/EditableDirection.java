package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class EditableDirection {

    private String explanation;
    private Parties parties;
    private String dateDue;

    private EditableDirection() {
        // noop -- for deserializer
    }

    public EditableDirection(
        String explanation,
        Parties parties,
        String dateDue
    ) {
        requireNonNull(explanation);
        requireNonNull(parties);
        requireNonNull(dateDue);

        this.explanation = explanation;
        this.parties = parties;
        this.dateDue = dateDue;
    }

    public String getExplanation() {
        requireNonNull(explanation);
        return explanation;
    }

    public Parties getParties() {
        requireNonNull(parties);
        return parties;
    }

    public String getDateDue() {
        requireNonNull(dateDue);
        return dateDue;
    }
}
