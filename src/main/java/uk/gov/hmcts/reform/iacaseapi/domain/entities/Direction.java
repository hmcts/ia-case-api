package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class Direction {

    private String explanation;
    private Parties parties;
    private String dateDue;
    private String dateSent;
    private DirectionTag directionTag;

    private Direction() {
        // noop -- for deserializer
    }

    public Direction(
        String explanation,
        Parties parties,
        String dateDue,
        String dateSent,
        DirectionTag directionTag
    ) {
        requireNonNull(explanation);
        requireNonNull(parties);
        requireNonNull(dateDue);
        requireNonNull(dateSent);
        requireNonNull(directionTag);

        this.explanation = explanation;
        this.parties = parties;
        this.dateDue = dateDue;
        this.dateSent = dateSent;
        this.directionTag = directionTag;
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

    public String getDateSent() {
        requireNonNull(dateSent);
        return dateSent;
    }

    public DirectionTag getDirectionTag() {
        requireNonNull(directionTag);
        return directionTag;
    }
}
