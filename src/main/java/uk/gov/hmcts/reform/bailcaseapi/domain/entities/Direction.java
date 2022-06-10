package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class Direction {

    private String sendDirectionDescription;
    private String sendDirectionList;
    private String dateOfCompliance;
    private String dateSent;
    private String dateTimeDirectionCreated;
    private String dateTimeDirectionModified;
    private List<IdValue<PreviousDates>> previousDates;


    private Direction() {
    }

    public Direction(
        String sendDirectionDescription,
        String sendDirectionList,
        String dateOfCompliance,
        String dateSent,
        String dateTimeDirectionCreated,
        String dateTimeDirectionModified,
        List<IdValue<PreviousDates>> previousDates

    ) {
        this.sendDirectionDescription = requireNonNull(sendDirectionDescription);
        this.sendDirectionList = requireNonNull(sendDirectionList);
        this.dateOfCompliance = requireNonNull(dateOfCompliance);
        this.dateSent = requireNonNull(dateSent);
        this.dateTimeDirectionCreated = requireNonNull(dateTimeDirectionCreated);
        this.dateTimeDirectionModified = dateTimeDirectionModified;
        this.previousDates = requireNonNull(previousDates);

    }


    public String getSendDirectionDescription() {
        return requireNonNull(sendDirectionDescription);
    }

    public String getSendDirectionList() {
        return requireNonNull(sendDirectionList);
    }

    public String getDateOfCompliance() {
        return requireNonNull(dateOfCompliance);
    }

    public String getDateSent() {
        return requireNonNull(dateSent);
    }

    public String getDateTimeDirectionCreated() {
        return dateTimeDirectionCreated;
    }

    public List<IdValue<PreviousDates>> getPreviousDates() {
        return requireNonNull(previousDates);
    }

    public String getDateTimeDirectionModified() {
        return dateTimeDirectionModified;
    }
}
