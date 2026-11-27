package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

import static java.util.Objects.requireNonNull;

public class PreviousDates  {

    private String dateDue;
    private String dateSent;

    @JsonCreator
    private PreviousDates() {
        // noop -- for deserializer
    }

    public PreviousDates(String dateDue, String dateSent) {
        requireNonNull(dateDue);
        requireNonNull(dateSent);

        this.dateDue = dateDue;
        this.dateSent = dateSent;
    }

    public String getDateDue() {
        requireNonNull(dateDue);
        return dateDue;
    }

    public String getDateSent() {
        requireNonNull(dateSent);
        return dateSent;
    }

}
